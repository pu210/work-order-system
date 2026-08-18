package com.eeit219.work_order_system.modules.c.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record ReviewAcceptRequest(
        @NotNull(message = "優先級不可為空") Integer priorityId,

        @NotNull(message = "指派工程師不可為空") Integer assignedHandlerId,

        @NotNull(message = "預計完成時間不可為空") LocalDateTime dueTime,

        String feedback) {
    public ReviewAcceptRequest {
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }

}
