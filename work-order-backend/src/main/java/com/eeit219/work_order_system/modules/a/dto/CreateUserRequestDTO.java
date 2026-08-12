package com.eeit219.work_order_system.modules.a.dto;

import java.util.List;

public record CreateUserRequestDTO(
                String account,
                String name,
                String email,
                String phone,
                String password,
                List<String> roleCodes) {
}
