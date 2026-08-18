package com.eeit219.work_order_system.modules.a.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.common.security.PasswordResetTokenUtility;
import com.eeit219.work_order_system.modules.a.dto.ForgotPasswordRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.ResetPasswordRequestDTO;
import com.eeit219.work_order_system.modules.a.entity.PasswordResetToken;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.PasswordResetTokenRepository;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;

@Service
@Transactional
public class PasswordResetService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenUtility tokenUtility;
    private final MailService mailService;
    private final String passwordResetUrl;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository, PasswordEncoder passwordEncoder,
            PasswordResetTokenUtility tokenUtility,
            MailService mailService, @Value("${app.password-reset-url}") String passwordResetUrl) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenUtility = tokenUtility;
        this.mailService = mailService;
        this.passwordResetUrl = passwordResetUrl;

    }

    // 申請忘記密碼
    public void requestPasswordReset(
            ForgotPasswordRequestDTO request) {

        if (request == null
                || request.email() == null
                || request.email().isBlank()) {
            throw new IllegalArgumentException("email為必填");
        }

        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email).orElse(null);
        System.out.println("找到使用者：" + (user != null));
        System.out.println("使用者狀態：" +
                (user == null ? null : user.getStatus()));

        if (user == null
                || user.getStatus() != User.UserStatus.ACTIVE) {
            return;
        }

        // 刪除該使用者之前申請的 Token
        passwordResetTokenRepository
                .deleteByUser_UserId(user.getUserId());

        // 原始 Token：只能放在信件連結
        String rawToken = tokenUtility.generateToken();

        // 雜湊 Token：存進資料庫
        String tokenHash = tokenUtility.hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(tokenHash);
        resetToken.setExpiresAt(
                LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(resetToken);

        String resetUrl = passwordResetUrl + "?token="
                + URLEncoder.encode(
                        rawToken,
                        StandardCharsets.UTF_8);
        mailService.sendPasswordResetEmail(
                user.getEmail(),
                resetUrl);
    }

    // 使用 token 重設密碼
    public void resetPassword(ResetPasswordRequestDTO request) {
        validateResetPasswordRequest(request);
        String tokenHash = tokenUtility.hashToken(
                request.token().trim());

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "重設連結無效或已過期"));

        LocalDateTime now = LocalDateTime.now();

        // 檢查 Token 是否已使用/過期
        if (resetToken.getUsedAt() != null
                || !resetToken.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException("重設連結無效或已過期");
        }

        User user = resetToken.getUser();

        // 確認帳號是否為 ACTIVE
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalArgumentException("此帳號目前無法重設密碼");
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.password()));
        user.setMustChangePassword(false);

        // Token 使用一次後立即失效
        resetToken.setUsedAt(now);

        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
    }

    // 驗證重設密碼 request
    private void validateResetPasswordRequest(
            ResetPasswordRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("請提供重設密碼資料");
        }

        if (request.token() == null || request.token().isBlank()) {
            throw new IllegalArgumentException("token為必填");
        }

        if (request.password() == null
                || request.password().isBlank()) {
            throw new IllegalArgumentException("password為必填");
        }

        if (request.confirmPassword() == null
                || request.confirmPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "confirmPassword為必填");
        }
        // 判斷 password、confirmPassword 是否相等
        if (!request.password().equals(
                request.confirmPassword())) {
            throw new IllegalArgumentException(
                    "兩次輸入的密碼不一致");
        }
    }
}