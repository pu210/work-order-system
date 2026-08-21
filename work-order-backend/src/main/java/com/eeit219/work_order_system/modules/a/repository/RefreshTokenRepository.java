package com.eeit219.work_order_system.modules.a.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.a.entity.RefreshToken;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}