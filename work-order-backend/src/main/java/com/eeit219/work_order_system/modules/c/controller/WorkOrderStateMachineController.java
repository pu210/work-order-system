package com.eeit219.work_order_system.modules.c.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.modules.c.dto.ReviewAcceptRequest;
import com.eeit219.work_order_system.modules.c.dto.ChangeStatusRequest;
import com.eeit219.work_order_system.modules.c.service.AdminCheckService;
import com.eeit219.work_order_system.modules.c.service.ProgressService;
import com.eeit219.work_order_system.modules.c.service.ReviewService;
import com.eeit219.work_order_system.modules.c.service.UserCheckService;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderStateMachineController {

    private final ReviewService reviewService;
    private final ProgressService progressService;
    private final UserCheckService userCheckService;
    private final AdminCheckService adminCheckService;

    public WorkOrderStateMachineController(
            ReviewService reviewService,
            UserCheckService userCheckService,
            ProgressService progressService,
            AdminCheckService adminCheckService) {
        this.reviewService = reviewService;
        this.userCheckService = userCheckService;
        this.progressService = progressService;
        this.adminCheckService = adminCheckService;
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
            @RequestBody ChangeStatusRequest request) {

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

    @PostMapping("/{workOrderId}/progress/accept")
    public Map<String, Object> progressAccept(
            @PathVariable Integer workOrderId,
            @RequestBody ChangeStatusRequest request) {
        if (request.userId() == null) {
            return Map.of("message", "使用者ID不可為空");
        }
        try {
            progressService.progressAccept(request, workOrderId);
            return Map.of("message", "success");
        } catch (Exception e) {
            return Map.of("message", e.getMessage());
        }
    }

    @PostMapping("/{workOrderId}/progress/reject")
    public Map<String, Object> progressReject(
            @PathVariable Integer workOrderId,
            @RequestBody ChangeStatusRequest request) {
        if (request.userId() == null) {
            return Map.of("message", "使用者ID不可為空");
        }
        if (request.feedback() == null) {
            return Map.of("message", "拒絕工單必須填寫反饋");
        }
        try {
            progressService.progressReject(request, workOrderId);
            return Map.of("message", "success");
        } catch (Exception e) {
            return Map.of("message", e.getMessage());
        }
    }

    @PostMapping("/{workOrderId}/usercheck/accept")
    public Map<String, Object> userCheckAccept(
            @PathVariable Integer workOrderId,
            @RequestBody ChangeStatusRequest request) {
        if (request.userId() == null) {
            return Map.of("message", "使用者ID不可為空");
        }
        try {
            userCheckService.userCheckAccept(request, workOrderId);
            return Map.of("message", "success");
        } catch (Exception e) {
            return Map.of("message", e.getMessage());
        }
    }

    @PostMapping("/{workOrderId}/admincheck/accept")
    public Map<String, Object> adminCheckAccept(
            @PathVariable Integer workOrderId,
            @RequestBody ChangeStatusRequest request) {
        if (request.userId() == null) {
            return Map.of("message", "使用者ID不可為空");
        }
        try {
            adminCheckService.adminCheckAccept(request, workOrderId);
            return Map.of("message", "success");
        } catch (Exception e) {
            return Map.of("message", e.getMessage());
        }
    }

    @PostMapping("/{workOrderId}/admincheck/reject")
    public Map<String, Object> adminCheckReject(
            @PathVariable Integer workOrderId,
            @RequestBody ChangeStatusRequest request) {
        if (request.userId() == null) {
            return Map.of("message", "使用者ID不可為空");
        }
        if (request.feedback() == null) {
            return Map.of("message", "拒絕工單必須填寫反饋");
        }
        try {
            adminCheckService.adminCheckReject(request, workOrderId);
            return Map.of("message", "success");
        } catch (Exception e) {
            return Map.of("message", e.getMessage());
        }
    }

}