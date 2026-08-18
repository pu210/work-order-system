package com.eeit219.work_order_system.modules.f.dto;

import lombok.Data;

@Data
public class RepairCategoryRequestDto {

    private String name;

    private Boolean status;

    private Integer defaultPriorityId;
}
