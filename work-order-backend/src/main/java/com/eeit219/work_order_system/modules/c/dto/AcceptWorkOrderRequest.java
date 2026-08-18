package com.eeit219.work_order_system.modules.c.dto;

public record AcceptWorkOrderRequest(
        String feedback) {
    public AcceptWorkOrderRequest {
        if (feedback == null || feedback.isBlank()) {
            feedback = null;
        } else {
            feedback = feedback.strip();
        }
    }
}
