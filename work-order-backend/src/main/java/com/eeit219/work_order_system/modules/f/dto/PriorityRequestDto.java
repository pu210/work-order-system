package com.eeit219.work_order_system.modules.f.dto;

import lombok.Data;

@Data
public class PriorityRequestDto {

    private String name;

    private Integer hours;

    private Boolean status; // 可選，預設可在 Entity 或 Service 處理
}
