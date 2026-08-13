package com.eeit219.work_order_system.modules.c.dto;

import jakarta.validation.constraints.NotNull;

public record AcceptWorkOrderRequest(
        @NotNull(message = "使用者ID不可為空") Integer userId,
        String feedback) {
    public AcceptWorkOrderRequest {
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }
}
