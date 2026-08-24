package com.eeit219.work_order_system.modules.f.dto;

import lombok.Data;

@Data
public class RepairTargetResponseDto {

    private Integer targetId;
    private String targetNo;
    private String name;
    private String model;
    private Boolean status; // 回傳給前端目前的狀態
}