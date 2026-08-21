package com.eeit219.work_order_system.modules.c.dto;

import java.time.LocalDateTime;

public record EditSessionResponse(
        String sessionToken,
        Integer editorUserId,
        String editorName,
        LocalDateTime startTime,
        LocalDateTime lastActiveTime,
        LocalDateTime expiresAt) {
}