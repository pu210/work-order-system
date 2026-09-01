package com.eeit219.work_order_system.modules.c.dto;

import java.util.List;
import java.util.Map;

public record AdminArchiveSuggestionResponse(
        String failureCause,
        String repairAction,
        String replacedParts,
        String testResult,
        List<String> insufficientFields,
        Map<String, List<String>> evidence,
        int sourceCount,
        Integer workOrderVersion,
        Integer latestHistoryId) {
}
