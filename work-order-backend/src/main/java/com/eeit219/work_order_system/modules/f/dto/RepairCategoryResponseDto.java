package com.eeit219.work_order_system.modules.f.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RepairCategoryResponseDto {

    private Integer repairCategoriesId;
    private String name;
    private Boolean status;
    private Integer defaultPriorityId;      // 預設優先級 ID
    private String defaultPriorityName;     // 預設優先級名稱 (對應 Entity 裡的 helper 方法)
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
