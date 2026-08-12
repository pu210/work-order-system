package com.eeit219.work_order_system.modules.a.dto;

import java.util.List;

public record ReviewUserResponseDTO(
        Integer userId,
        String account,
        String name,
        String email,
        Byte status,
        List<String> roleCodes) {
}
