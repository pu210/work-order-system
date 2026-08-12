package com.eeit219.work_order_system.modules.a.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.a.entity.UserOauthAccount;

public interface UserOauthAccountRepository
        extends JpaRepository<UserOauthAccount, Integer> {

    Optional<UserOauthAccount> findByProviderAndProviderUserId(
            String provider,
            String providerUserId);

    Optional<UserOauthAccount> findByProviderAndEmail(
            String provider,
            String email);

    List<UserOauthAccount> findByUser_UserId(Integer userId);

    boolean existsByProviderAndProviderUserId(
            String provider,
            String providerUserId);

    void deleteByUser_UserIdAndProvider(Integer userId, String provider);
}
