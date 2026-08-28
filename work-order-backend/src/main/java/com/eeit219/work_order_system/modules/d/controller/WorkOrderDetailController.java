package com.eeit219.work_order_system.modules.d.controller;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderDetailResponse;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderFeedbackRecordResponse;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderRejectionRecordResponse;
import com.eeit219.work_order_system.modules.d.service.WorkOrderDetailService;
import com.eeit219.work_order_system.modules.d.service.WorkOrderFeedbackRecordService;
import com.eeit219.work_order_system.modules.d.service.WorkOrderRejectionRecordService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-orders/{workOrderId}")
@RequiredArgsConstructor
public class WorkOrderDetailController {

    private final WorkOrderDetailService workOrderDetailService;
    private final WorkOrderRejectionRecordService workOrderRejectionRecordService;
    private final WorkOrderFeedbackRecordService workOrderFeedbackRecordService;
    private final UserRepository userRepository;

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<WorkOrderDetailResponse>> getDetail(
            @PathVariable Integer workOrderId,
            Authentication authentication) {
        // 取得登入者並呼叫 Service
        User currentUser = getCurrentUser(authentication);

        WorkOrderDetailResponse response =
                workOrderDetailService.getWorkOrderDetail(
                        workOrderId,
                        currentUser
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "取得工單詳情成功",
                        response
                )
        );
    }

    /**
     * 取得目前使用者有權查看的退回紀錄。
     */
    @GetMapping("/rejection-records")
    public ResponseEntity<ApiResponse<List<WorkOrderRejectionRecordResponse>>> getRejectionRecords(
            @PathVariable Integer workOrderId,
            Authentication authentication) {

        List<WorkOrderRejectionRecordResponse> records =
                workOrderRejectionRecordService.getVisibleRejectionRecords(
                        workOrderId,
                        getCurrentUser(authentication));

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "取得工單退回紀錄成功",
                        records));
    }

    /**
     * 取得工單所有接受流程回饋，僅限管理員。
     */
    @GetMapping("/feedback-records")
    public ResponseEntity<ApiResponse<List<WorkOrderFeedbackRecordResponse>>> getFeedbackRecords(
            @PathVariable Integer workOrderId,
            Authentication authentication) {

        List<WorkOrderFeedbackRecordResponse> records =
                workOrderFeedbackRecordService.getAllFeedbackRecords(
                        workOrderId,
                        getCurrentUser(authentication));

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "取得工單流程回饋成功",
                        records));
    }

    /**
     * 依登入帳號取得目前使用者，供同一 Controller 的查詢端點共用。
     */
    private User getCurrentUser(Authentication authentication) {
        return userRepository
                .findByAccount(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "找不到使用者：" + authentication.getName()));
    }

}
