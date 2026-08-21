package com.eeit219.work_order_system.modules.a.dto;

public record UpdateProfileRequestDTO(
        String name,
        String email,
        String phone) {
}