package com.eeit219.work_order_system.modules.c.dto;

public record ChangeStatusRequest(
        Integer userId,
        String feedback) {
    public ChangeStatusRequest {
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }
}
