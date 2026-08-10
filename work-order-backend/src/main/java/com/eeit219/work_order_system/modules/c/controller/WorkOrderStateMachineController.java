package com.eeit219.work_order_system.modules.c.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.modules.c.dto.ReviewAcceptRequest;
import com.eeit219.work_order_system.modules.c.dto.ReviewRejectRequest;
import com.eeit219.work_order_system.modules.c.service.ReviewService;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderStateMachineController {

    private final ReviewService reviewService;

    public WorkOrderStateMachineController(
            ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{workOrderId}/review/accept")
    public Map<String, Object> reviewAccept(
            @PathVariable Integer workOrderId,
            @RequestBody ReviewAcceptRequest request) {

        if (request.userId() == null) {
            return Map.of("message", "使用者ID不可為空");
        }

        if (request.priorityId() == null) {
            return Map.of("message", "優先級不可為空");
        }

        if (request.assignedHandler() == null) {
            return Map.of("message", "指派工程師不可為空");
        }

        if (request.dueTime() == null) {
            return Map.of("message", "預計完成時間不可為空");
        }
        try {
            reviewService.reviewAccept(request, workOrderId);
            return Map.of("message", "success");
        } catch (Exception e) {
            return Map.of("message", e.getMessage());
        }
    }

    @PostMapping("/{workOrderId}/review/reject")
    public Map<String, Object> reviewReject(
            @PathVariable Integer workOrderId,
            @RequestBody ReviewRejectRequest request) {

        if (request.userId() == null) {
            return Map.of("message", "使用者ID不可為空");
        }

        if (request.feedback() == null) {
            return Map.of("message", "拒絕工單必須填寫反饋");
        }
        try {
            reviewService.reviewReject(request, workOrderId);
            return Map.of("message", "success");
        } catch (Exception e) {
            return Map.of("message", e.getMessage());
        }
    }
}