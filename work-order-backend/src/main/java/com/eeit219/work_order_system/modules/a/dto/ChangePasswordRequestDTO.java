package com.eeit219.work_order_system.modules.a.dto;

public record ChangePasswordRequestDTO(
        String currentPassword,
        String newPassword,
        String confirmPassword) {

}
