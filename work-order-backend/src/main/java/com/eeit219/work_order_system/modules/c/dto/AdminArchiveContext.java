package com.eeit219.work_order_system.modules.c.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminArchiveContext(
        String sourceId,
        Integer workOrderId,
        String workOrderNo,
        String title,
        String description,
        Integer workOrderVersion,
        Integer latestHistoryId,
        List<HistorySource> timeline) {

    public record HistorySource(
            String sourceId,
            String stage,
            String status,
            String event,
            LocalDateTime editedTime,
            String feedback) {
    }
}
