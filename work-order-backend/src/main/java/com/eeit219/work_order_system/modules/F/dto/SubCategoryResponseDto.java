package com.eeit219.work_order_system.modules.F.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SubCategoryResponseDto {

    private Integer subCategoriesId;
    private Integer categoryId;
    private String categoryName; // 所屬大類名稱
    private String name;
    private Integer overridePriorityId; // 特例優先級 ID (若有)
    private String overridePriorityName; // 特例優先級名稱
    private Integer effectivePriorityId; // 【核心】最終生效的優先級 ID (有特例用特例，沒有就用大類預設)
    private Boolean status;
    private LocalDateTime created_time;
    private LocalDateTime updated_time;
}
