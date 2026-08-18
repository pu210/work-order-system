package com.eeit219.work_order_system.modules.f.dto;

import lombok.Data;

@Data
public class PriorityResponseDto {

    private Integer prioritiesId;
    private String name;
    private Integer hours;
    private Boolean status;
}
