package com.eeit219.work_order_system.modules.c.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProgressAcceptRequest(
        @NotNull(message = "設備編號不可為空") String targetNo,
        @NotBlank(message = "回報完成時必須填寫處理反饋") String feedback) {
    public ProgressAcceptRequest {
        if (targetNo == null || targetNo.isBlank()) {
            targetNo = null;
        } else {
            targetNo = targetNo.strip();
        }
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }
}
