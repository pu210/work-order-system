package com.eeit219.work_order_system.modules.b.controller;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.response.PageResponse;
import com.eeit219.work_order_system.common.security.CurrentUserProvider;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderListItemResponse;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;
import com.eeit219.work_order_system.modules.b.service.WorkOrderCreationCoordinator;
import com.eeit219.work_order_system.modules.b.service.WorkOrderService;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private static final int DEFAULT_PAGE_SIZE = WorkOrderPageableFactory.DEFAULT_PAGE_SIZE;

    private final WorkOrderService workOrderService;
    private final WorkOrderCreationCoordinator workOrderCreationCoordinator;
    private final CurrentUserProvider currentUserProvider;

    public WorkOrderController(WorkOrderService workOrderService,
            WorkOrderCreationCoordinator workOrderCreationCoordinator,
            CurrentUserProvider currentUserProvider) {
        this.workOrderService = workOrderService;
        this.workOrderCreationCoordinator = workOrderCreationCoordinator;
        this.currentUserProvider = currentUserProvider;
    }

    // 建單與附件改成同一支 API、同一個交易：request 走 JSON part，files 走檔案 part（可不帶）。
    // 重試邏輯見 WorkOrderCreationCoordinator（只重試工單編號撞號，附件驗證失敗不重試、直接失敗）
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<WorkOrderResponse>> create(
            @Valid @RequestPart("request") WorkOrderCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        WorkOrderResponse response = workOrderCreationCoordinator.createWithRetry(request,
                currentUserProvider.getUser(), files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "工單建立成功", response));
    }

    // 工單編號重試 MAX_CREATE_RETRY 次仍撞號（極端併發情況），視為資源衝突回 409。
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleCreateConflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), "工單編號產生衝突，請重新嘗試"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> getById(@PathVariable Integer id) {
        WorkOrderResponse response = workOrderService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "成功", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WorkOrderListItemResponse>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WorkOrderState status,
            @RequestParam(required = false) Integer priorityId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer assignedHandlerId,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = WorkOrderPageableFactory.of(page, size, sort);
        Page<WorkOrderListItemResponse> response = workOrderService.list(keyword, status, priorityId,
                categoryId,
                assignedHandlerId, currentUserProvider.getUserId(), currentUserProvider.getRoleCodes(), pageable);
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK.value(), "成功", PageResponse.from(response)));
    }

    @GetMapping("/my-submissions")
    public ResponseEntity<ApiResponse<PageResponse<WorkOrderListItemResponse>>> mySubmissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WorkOrderState status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        Pageable pageable = WorkOrderPageableFactory.of(page, size);
        Page<WorkOrderListItemResponse> response = workOrderService.listMySubmissions(keyword, status,
                currentUserProvider.getUserId(), pageable);
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK.value(), "成功", PageResponse.from(response)));
    }

}
