package com.eeit219.work_order_system.modules.c.dto;

import java.time.LocalDateTime;

public record ReviewAcceptRequest(
        Integer userId,
        Integer priorityId,
        Integer assignedHandler,
        LocalDateTime dueTime,
        String feedback) {
    public ReviewAcceptRequest {
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }
}
