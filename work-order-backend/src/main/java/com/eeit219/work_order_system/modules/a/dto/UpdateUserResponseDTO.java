package com.eeit219.work_order_system.modules.a.dto;

import java.util.List;

public record UpdateUserResponseDTO(
                Integer userId,
                String account,
                String name,
                String email,
                String phone,
                Byte status,
                List<String> roleCodes) {
}
