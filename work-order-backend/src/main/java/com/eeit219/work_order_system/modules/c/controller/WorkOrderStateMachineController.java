package com.eeit219.work_order_system.modules.c.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.security.AuthenticatedUser;
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
                        @Valid @RequestBody ReviewAcceptRequest request,
                        @AuthenticationPrincipal AuthenticatedUser loginUser) {

                reviewService.reviewAccept(request, workOrderId, loginUser.userId());
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "null",
                                                null));
        }

        @PostMapping("/{workOrderId}/review/reject")
        public ResponseEntity<ApiResponse<Object>> reviewReject(
                        @PathVariable Integer workOrderId,
                        @Valid @RequestBody RejectWorkOrderRequest request,
                        @AuthenticationPrincipal AuthenticatedUser loginUser) {

                reviewService.reviewReject(request, workOrderId, loginUser.userId());
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "null",
                                                null));
        }

        @PostMapping("/{workOrderId}/progress/accept")
        public ResponseEntity<ApiResponse<Object>> progressAccept(
                        @PathVariable Integer workOrderId,
                        @Valid @RequestBody AcceptWorkOrderRequest request,
                        @AuthenticationPrincipal AuthenticatedUser loginUser) {

                progressService.progressAccept(request, workOrderId, loginUser.userId());
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "null",
                                                null));
        }

        @PostMapping("/{workOrderId}/progress/reject")
        public ResponseEntity<ApiResponse<Object>> progressReject(
                        @PathVariable Integer workOrderId,
                        @Valid @RequestBody RejectWorkOrderRequest request,
                        @AuthenticationPrincipal AuthenticatedUser loginUser) {

                progressService.progressReject(request, workOrderId, loginUser.userId());
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "null",
                                                null));
        }

        @PostMapping("/{workOrderId}/user-check/accept")
        public ResponseEntity<ApiResponse<Object>> userCheckAccept(
                        @PathVariable Integer workOrderId,
                        @Valid @RequestBody AcceptWorkOrderRequest request,
                        @AuthenticationPrincipal AuthenticatedUser loginUser) {

                userCheckService.userCheckAccept(request, workOrderId, loginUser.userId());
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "null",
                                                null));
        }

        @PostMapping("/{workOrderId}/admin-check/accept")
        public ResponseEntity<ApiResponse<Object>> adminCheckAccept(
                        @PathVariable Integer workOrderId,
                        @Valid @RequestBody AcceptWorkOrderRequest request,
                        @AuthenticationPrincipal AuthenticatedUser loginUser) {

                adminCheckService.adminCheckAccept(request, workOrderId, loginUser.userId());
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "null",
                                                null));
        }

        @PostMapping("/{workOrderId}/admin-check/reject")
        public ResponseEntity<ApiResponse<Object>> adminCheckReject(
                        @PathVariable Integer workOrderId,
                        @Valid @RequestBody RejectWorkOrderRequest request,
                        @AuthenticationPrincipal AuthenticatedUser loginUser) {

                adminCheckService.adminCheckReject(request, workOrderId, loginUser.userId());
                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "null",
                                                null));
        }

}
