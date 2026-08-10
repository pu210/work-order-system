package com.eeit219.work_order_system.modules.c.dto;

import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;

public record ChangeWorkOrderStateRequest(
                Integer workOrderId,
                Integer userId,
                Integer priorityId,
                Integer assignedHandler,
                java.time.LocalDateTime dueTime,
                String feedback,
                WorkOrderEvent event) {
        public ChangeWorkOrderStateRequest {

                // 如果傳進來的是 null、空字串 ""、或是只有空白 " "
                if (feedback == null || feedback.trim().isEmpty()) {
                        feedback = null;
                } else {
                        feedback = feedback.trim(); // 順便把前後空白清掉（選填）
                }
        }
}