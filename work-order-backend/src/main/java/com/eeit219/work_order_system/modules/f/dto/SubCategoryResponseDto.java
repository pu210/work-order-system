package com.eeit219.work_order_system.modules.f.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SubCategoryResponseDto {

    private Integer subCategoriesId;
    private Integer categoryId;
    private String categoryName;           // 所屬大類名稱
    private String name;
    private Integer overridePriorityId;    // 特例優先級 ID (若有)
    private String overridePriorityName;   // 特例優先級名稱
    private Integer effectivePriorityId;   // 最終生效的優先級 ID
    private String effectivePriorityName;  // 🌟 補上這一行！
    private Boolean status;
    private LocalDateTime createdTime;     // 統一用駝峰命名
    private LocalDateTime updatedTime;     // 統一用駝峰命名
}
