package com.eeit219.work_order_system.modules.a.dto;

import java.util.List;

public record ReviewUserRequestDTO(
        Boolean approved,
        List<String> roleCodes) {
}
