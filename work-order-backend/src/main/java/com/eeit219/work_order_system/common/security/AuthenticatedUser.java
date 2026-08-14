package com.eeit219.work_order_system.common.security;

import org.springframework.security.core.AuthenticatedPrincipal;

public record AuthenticatedUser(
        Integer userId,
        String account
) implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return account;
    }
}
