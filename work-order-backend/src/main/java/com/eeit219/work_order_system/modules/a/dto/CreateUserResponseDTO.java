package com.eeit219.work_order_system.modules.a.dto;

import java.util.List;

public record CreateUserResponseDTO(
                String account,
                String name,
                String email,
                String phone,
                List<String> roleCodes,
                Byte status,
                Boolean mustChangePassword) {
}