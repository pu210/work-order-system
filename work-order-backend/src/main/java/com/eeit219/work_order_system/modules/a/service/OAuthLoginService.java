package com.eeit219.work_order_system.modules.a.service;

import java.util.List;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.a.dto.CurrentUserDTO;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.entity.UserOauthAccount;
import com.eeit219.work_order_system.modules.a.repository.UserOauthAccountRepository;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.a.repository.UserRoleRepository;

@Service
@Transactional
public class OAuthLoginService {

    private static final String PROVIDER_GOOGLE = "google";

    private final UserRepository userRepository;
    private final UserOauthAccountRepository oauthAccountRepository;
    private final UserRoleRepository userRoleRepository;

    public OAuthLoginService(
            UserRepository userRepository,
            UserOauthAccountRepository oauthAccountRepository,
            UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
        this.userRoleRepository = userRoleRepository;
    }

    // 從 Google 登入
    public CurrentUserDTO loginWithGoogle(OAuth2User oauth2User) {
        String providerUserId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        Boolean emailVerified = oauth2User.getAttribute("email_verified");

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("Google 帳號缺少識別碼");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google 帳號未提供 Email");
        }

        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new IllegalArgumentException("Google Email 尚未驗證");
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = oauthAccountRepository
                .findByProviderAndProviderUserId(
                        PROVIDER_GOOGLE,
                        providerUserId)
                .map(UserOauthAccount::getUser)
                .orElseGet(() -> linkExistingUser(
                        providerUserId,
                        normalizedEmail));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalArgumentException("此系統帳號尚未啟用");
        }

        List<String> roleCodes = userRoleRepository
                .findRoleCodesByUserId(user.getUserId())
                .stream()
                .map(role -> role.trim().toUpperCase())
                .distinct()
                .sorted()
                .toList();

        return new CurrentUserDTO(
                user.getUserId(),
                user.getAccount(),
                user.getName(),
                user.getEmail(),
                false,
                roleCodes);
    }

    private User linkExistingUser(
            String providerUserId,
            String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "此 Google Email 尚未建立系統帳號"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalArgumentException("此系統帳號尚未啟用");
        }

        UserOauthAccount oauthAccount = new UserOauthAccount();
        oauthAccount.setUser(user);
        oauthAccount.setProvider(PROVIDER_GOOGLE);
        oauthAccount.setProviderUserId(providerUserId);
        oauthAccount.setEmail(email);

        // 登入用途不需要保存 Google access token
        oauthAccountRepository.save(oauthAccount);

        return user;
    }
}