package com.eeit219.work_order_system.modules.d.controller;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordCreateRequest;
import com.eeit219.work_order_system.modules.d.dto.ContactRecordResponse;
import com.eeit219.work_order_system.modules.d.service.ContactRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders/{workOrderId}/contact-records")
@RequiredArgsConstructor
public class ContactRecordController {

    private final ContactRecordService contactRecordService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactRecordResponse>>> getRecords(
            @PathVariable Integer workOrderId,
            Authentication authentication) {

        List<ContactRecordResponse> records = contactRecordService.getRecords(
                workOrderId,
                getCurrentUser(authentication)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "取得聯絡紀錄成功",
                records
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactRecordResponse>> createComment(
            @PathVariable Integer workOrderId,
            @Valid @RequestBody ContactRecordCreateRequest request,
            Authentication authentication) {

        ContactRecordResponse record = contactRecordService.createComment(
                workOrderId,
                request,
                getCurrentUser(authentication)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                "新增留言成功",
                record
        ));
    }

    private User getCurrentUser(Authentication authentication) {
        String account = authentication.getName();
        return userRepository.findByAccount(account)
                .orElseThrow(() -> new IllegalStateException("找不到使用者：" + account));
    }
}
