package com.eeit219.work_order_system.modules.a.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.common.security.RefreshTokenUtility;
import com.eeit219.work_order_system.modules.a.entity.RefreshToken;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.RefreshTokenRepository;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenUtility refreshTokenUtility;
    private final long expireDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            RefreshTokenUtility refreshTokenUtility,
            @Value("${refresh.token.expire-days}") long expireDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenUtility = refreshTokenUtility;
        this.expireDays = expireDays;
    }

    @Transactional
    public String createRefreshToken(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

        String rawToken = refreshTokenUtility.generateToken();
        String tokenHash = refreshTokenUtility.hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresTime(
                LocalDateTime.now().plusDays(expireDays));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public Optional<RotationResult> rotateRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        String tokenHash = refreshTokenUtility.hashToken(rawToken);

        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByTokenHash(tokenHash);

        if (optionalToken.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken currentToken = optionalToken.get();

        if (Boolean.TRUE.equals(currentToken.getRevoked())) {
            return Optional.empty();
        }

        if (!currentToken.getExpiresTime().isAfter(LocalDateTime.now())) {
            return Optional.empty();
        }
        User user = currentToken.getUser();

        if (user.getStatus() == null
                || user.getStatus() != User.UserStatus.ACTIVE) {
            return Optional.empty();
        }

        // 舊 token 立即失效
        currentToken.setRevoked(true);
        refreshTokenRepository.save(currentToken);

        Integer userId = currentToken.getUser().getUserId();
        String newRawToken = createRefreshToken(userId);

        return Optional.of(
                new RotationResult(userId, newRawToken));
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = refreshTokenUtility.hashToken(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    public record RotationResult(
            Integer userId,
            String refreshToken) {
    }
}