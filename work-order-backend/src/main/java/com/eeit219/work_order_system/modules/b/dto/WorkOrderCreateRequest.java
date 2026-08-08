package com.eeit219.work_order_system.modules.b.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderCreateRequest {
    private String title;
    private Integer subCategoryId;
    private String locationDetail;
    private String contactPhone;
    private String description;
}
