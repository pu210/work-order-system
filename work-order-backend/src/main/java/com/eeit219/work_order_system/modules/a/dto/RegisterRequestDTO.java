package com.eeit219.work_order_system.modules.a.dto;

import jakarta.validation.constraints.Pattern;

public record RegisterRequestDTO(
        String account,
        String password,
        String confirmPassword,
        String name,
        String email,
        @Pattern(regexp = "^$|^\\d{10}$", message = "電話需為 10 碼數字")
        String phone) {
}
