package com.eeit219.work_order_system.modules.d.dto;

import java.time.LocalDateTime;

import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 管理員查看的工單流程回饋紀錄。
 */
@Getter
@Builder
@AllArgsConstructor
public class WorkOrderFeedbackRecordResponse {

    private Integer historyId;
    private WorkOrderFeedbackType feedbackType;
    private Integer submittedByUserId;
    private String submittedByName;
    private String feedback;
    private LocalDateTime submittedTime;
    private WorkOrderState resultingStatus;
}
