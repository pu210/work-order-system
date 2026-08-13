package com.eeit219.work_order_system.modules.b.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class WorkOrderPageableFactory {

    // 暫時
    static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private WorkOrderPageableFactory() {
    }

    static Pageable of(int page, int size) {
        return PageRequest.of(page, clampSize(size));
    }

    static Pageable of(int page, int size, String sort) {
        return PageRequest.of(page, clampSize(size), resolveSort(sort));
    }

    private static int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private static Sort resolveSort(String sort) {
        if (sort == null) {
            return Sort.unsorted();
        }
        return switch (sort) {
            case "PRIORITY_HIGH_TO_LOW" -> Sort.by(Sort.Direction.ASC, "priority.hours");
            case "PRIORITY_LOW_TO_HIGH" -> Sort.by(Sort.Direction.DESC, "priority.hours");
            case "DUE_TIME_NEAR_TO_FAR" -> Sort.by(Sort.Direction.ASC, "dueTime");
            case "DUE_TIME_FAR_TO_NEAR" -> Sort.by(Sort.Direction.DESC, "dueTime");
            case "WORK_ORDER_NO_ASC" -> Sort.by(Sort.Direction.ASC, "workOrderNo");
            default -> Sort.unsorted();
        };
    }
}
