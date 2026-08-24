package com.eeit219.work_order_system.modules.a.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponseDTO(
        Integer userId,
        String account,
        String name,
        String email,
        String phone,
        Byte status,
        Boolean mustChangePassword,
        List<String> roleCodes,
        Boolean lastActiveAdmin,
        LocalDateTime createdTime,
        LocalDateTime updatedTime) {
}
