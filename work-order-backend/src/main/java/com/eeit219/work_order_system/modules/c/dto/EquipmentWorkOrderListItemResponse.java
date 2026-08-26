package com.eeit219.work_order_system.modules.c.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EquipmentWorkOrderListItemResponse {

    private Integer workOrderId;
    private String workOrderNo;
    private String title;

    private String categoryName;
    private String priorityName;
    private String status;

    private String creatorName;
    private String assignedHandlerName;

    private LocalDateTime createdTime;
    private LocalDateTime dueTime;
    private Boolean isOverdue;
}