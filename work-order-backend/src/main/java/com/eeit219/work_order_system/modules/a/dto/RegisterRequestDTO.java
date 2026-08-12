package com.eeit219.work_order_system.modules.a.dto;

public record RegisterRequestDTO(
        String account,
        String password,
        String confirmPassword,
        String name,
        String email,
        String phone) {
}