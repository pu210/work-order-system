package com.eeit219.work_order_system.modules.b.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class WorkOrderPageableFactory {

    // 暫時
    static final int DEFAULT_PAGE_SIZE = 15;
    private static final int MAX_PAGE_SIZE = 100;

    // 預設排序：建立時間新到舊，符合使用者直覺，未指定或指定無效的 sort 值時皆套用
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdTime");

    private WorkOrderPageableFactory() {
    }

    static Pageable of(int page, int size) {
        return PageRequest.of(page, clampSize(size), DEFAULT_SORT);
    }

    static Pageable of(int page, int size, String sort) {
        return PageRequest.of(page, clampSize(size), resolveSort(sort));
    }

    private static int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private static Sort resolveSort(String sort) {
        if (sort == null) {
            return DEFAULT_SORT;
        }
        return switch (sort) {
            case "CREATED_TIME_ASC" -> Sort.by(Sort.Direction.ASC, "createdTime");
            case "CREATED_TIME_DESC" -> Sort.by(Sort.Direction.DESC, "createdTime");
            case "WORK_ORDER_NO_ASC" -> Sort.by(Sort.Direction.ASC, "workOrderNo");
            default -> DEFAULT_SORT;
        };
    }
}
