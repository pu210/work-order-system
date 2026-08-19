package com.eeit219.work_order_system.modules.d.controller;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderDetailResponse;
import com.eeit219.work_order_system.modules.d.service.WorkOrderDetailService;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<WorkOrderDetailResponse>> getDetail(
            @PathVariable Integer workOrderId,
            Authentication authentication) {
        // 取得登入者並呼叫 Service
        User currentUser = userRepository
                .findByAccount(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("找不到使用者：" + authentication.getName()));

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

}
