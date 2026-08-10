package com.eeit219.work_order_system.modules.c.dto;

public record ReviewRejectRequest(
        Integer userId,
        String feedback) {
    public ReviewRejectRequest {
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }
}
