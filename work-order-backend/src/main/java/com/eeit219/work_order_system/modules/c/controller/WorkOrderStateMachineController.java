package com.eeit219.work_order_system.modules.c.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.c.dto.AcceptWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.dto.RejectWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.dto.ReviewAcceptRequest;
import com.eeit219.work_order_system.modules.c.service.AdminCheckService;
import com.eeit219.work_order_system.modules.c.service.ProgressService;
import com.eeit219.work_order_system.modules.c.service.ReviewService;
import com.eeit219.work_order_system.modules.c.service.UserCheckService;

import jakarta.validation.Valid;

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
    public ResponseEntity<ApiResponse<Object>> reviewAccept(
            @PathVariable Integer workOrderId,
            @Valid @RequestBody ReviewAcceptRequest request) {

        reviewService.reviewAccept(request, workOrderId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "null",
                        null));
    }

    @PostMapping("/{workOrderId}/review/reject")
    public ResponseEntity<ApiResponse<Object>> reviewReject(
            @PathVariable Integer workOrderId,
            @Valid @RequestBody RejectWorkOrderRequest request) {

        reviewService.reviewReject(request, workOrderId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "null",
                        null));
    }

    @PostMapping("/{workOrderId}/progress/accept")
    public ResponseEntity<ApiResponse<Object>> progressAccept(
            @PathVariable Integer workOrderId,
            @Valid @RequestBody AcceptWorkOrderRequest request) {

        progressService.progressAccept(request, workOrderId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "null",
                        null));
    }

    @PostMapping("/{workOrderId}/progress/reject")
    public ResponseEntity<ApiResponse<Object>> progressReject(
            @PathVariable Integer workOrderId,
            @Valid @RequestBody RejectWorkOrderRequest request) {

        progressService.progressReject(request, workOrderId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "null",
                        null));
    }

    @PostMapping("/{workOrderId}/usercheck/accept")
    public ResponseEntity<ApiResponse<Object>> userCheckAccept(
            @PathVariable Integer workOrderId,
            @Valid @RequestBody AcceptWorkOrderRequest request) {

        userCheckService.userCheckAccept(request, workOrderId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "null",
                        null));
    }

    @PostMapping("/{workOrderId}/admincheck/accept")
    public ResponseEntity<ApiResponse<Object>> adminCheckAccept(
            @PathVariable Integer workOrderId,
            @Valid @RequestBody AcceptWorkOrderRequest request) {

        adminCheckService.adminCheckAccept(request, workOrderId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "null",
                        null));
    }

    @PostMapping("/{workOrderId}/admincheck/reject")
    public ResponseEntity<ApiResponse<Object>> adminCheckReject(
            @PathVariable Integer workOrderId,
            @Valid @RequestBody RejectWorkOrderRequest request) {

        adminCheckService.adminCheckReject(request, workOrderId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "null",
                        null));
    }

}
