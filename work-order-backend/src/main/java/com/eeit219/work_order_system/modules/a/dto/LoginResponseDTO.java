package com.eeit219.work_order_system.modules.a.dto;

import java.util.List;

public record LoginResponseDTO(
                String token,
                String account,
                String email,
                List<String> roleCodes) {
}
