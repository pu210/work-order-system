package com.eeit219.work_order_system.modules.b.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class WorkOrderSummary {
    private Integer workOrderId;
    private String workOrderNo;
    private String title;
    private String categoryName;
    private String priorityName;
    private String status;
    private Integer creatorId;
    private String creatorName;
    private Integer adminUserId;

    // 負責管理員姓名
    private String adminName;

    // 預計完成期限
    private LocalDateTime dueTime;

    // 是否逾期
    private Boolean isOverdue;
    private Integer assignedHandlerId;
    private LocalDateTime createdTime;

}
