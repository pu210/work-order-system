package com.eeit219.work_order_system.modules.c.dto;

import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;

public record ChangeWorkOrderStateRequest(
        Integer workOrderId,
        Integer userId,
        Integer priorityId,
        Integer assignedHandler,
        java.time.LocalDateTime dueTime,
        WorkOrderEvent event) {
}