package com.eeit219.work_order_system.modules.c.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectWorkOrderRequest(
        @NotBlank(message = "退回或拒絕工單必須填寫原因")
        @Size(max = 500, message = "退回或拒絕原因不可超過 500 字")
        String feedback) {
    public RejectWorkOrderRequest {
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }
}
