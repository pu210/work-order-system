package com.eeit219.work_order_system.modules.a.dto;

import java.util.List;

import jakarta.validation.constraints.Pattern;

public record CreateUserRequestDTO(
        String account,
        String name,
        String email,
        @Pattern(regexp = "^$|^\\d{10}$", message = "聯絡電話需為 10 碼數字") String phone,
        String password,
        List<String> roleCodes) {
}
