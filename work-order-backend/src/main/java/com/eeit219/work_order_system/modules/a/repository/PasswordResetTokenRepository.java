package com.eeit219.work_order_system.modules.a.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eeit219.work_order_system.modules.a.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findFirstByUser_UserIdAndUsedAtIsNullOrderByCreatedTimeDesc(Integer userId);

    void deleteByUser_UserId(Integer userId);
}
