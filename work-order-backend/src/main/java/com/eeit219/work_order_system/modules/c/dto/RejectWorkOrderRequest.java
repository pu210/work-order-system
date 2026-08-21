package com.eeit219.work_order_system.modules.c.dto;

import jakarta.validation.constraints.NotNull;

public record RejectWorkOrderRequest(
        @NotNull(message = "拒絕工單必須填寫反饋") String feedback) {
    public RejectWorkOrderRequest {
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }
}
