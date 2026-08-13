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
public class WorkOrderListItemResponse {
    private Integer workOrderId;
    private String workOrderNo;
    private String title;
    private String categoryName;
    private String priorityName;
    private String status;
    private String creatorName;
    private String assignedHandlerName;
    private LocalDateTime createdTime;
}
