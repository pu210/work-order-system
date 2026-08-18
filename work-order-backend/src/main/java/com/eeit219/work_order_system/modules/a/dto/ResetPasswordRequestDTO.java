package com.eeit219.work_order_system.modules.a.dto;

public record ResetPasswordRequestDTO(
                String token,
                String password,
                String confirmPassword) {
}