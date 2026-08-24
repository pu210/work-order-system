package com.eeit219.work_order_system.modules.d.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class WorkOrderDetailResponse {
    private Integer workOrderId;
    private String workOrderNo;
    private String title;

    private String status;
    private String categoryName;
    private String subCategoryName;
    private String priorityName;

    private String locationDetail;
    private String description;

    private Integer creatorUserId;
    private String creatorName;
    private String contactPhone;

    private Integer assignedHandlerId;
    private String assignedHandlerName;
    private String assignedHandlerDepartment;

    private LocalDateTime dueTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer version;
    private Boolean isOverDue;

    private Integer version;
}
