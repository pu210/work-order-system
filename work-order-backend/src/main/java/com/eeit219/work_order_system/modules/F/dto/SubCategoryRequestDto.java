package com.eeit219.work_order_system.modules.F.dto;

import lombok.Data;

@Data
public class SubCategoryRequestDto {

    private Integer categoryId;
    private String name;
    private Integer overridePriorityId; // 可以傳 null (代表繼承大類預設)
    private Boolean status;
}
