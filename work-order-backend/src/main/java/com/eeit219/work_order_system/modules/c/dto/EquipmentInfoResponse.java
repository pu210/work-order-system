package com.eeit219.work_order_system.modules.c.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EquipmentInfoResponse {

    private Integer targetId;
    private String targetNo;
    private String name;
    private String model;
    private Boolean status;
}