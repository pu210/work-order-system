package com.eeit219.work_order_system.modules.b.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class WorkOrderResponse {
    private Integer workOrderId;
    private String workOrderNo;
    private String title;
    private String subCategoryName;
    private String priorityName;
    private String locationDetail;
    private String contactPhone;
    private String description;
    private LocalDateTime dueTime;
    private String status;
    private LocalDateTime createdTime;
    private String creatorName;
}
