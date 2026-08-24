package com.eeit219.work_order_system.modules.f.dto;

import lombok.Data;

@Data
public class RepairTargetRequestDto {

    private String targetNo;
    private String name;
    private String model;
    private Boolean status; // 接收前端傳來的狀態 (啟用/停用)
}