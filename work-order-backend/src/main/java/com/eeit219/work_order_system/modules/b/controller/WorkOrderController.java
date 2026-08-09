package com.eeit219.work_order_system.modules.b.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderListItemResponse;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;
import com.eeit219.work_order_system.modules.b.service.WorkOrderService;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private static final int PAGE_SIZE = 10;

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    public ApiResponse<WorkOrderResponse> create(@RequestBody WorkOrderCreateRequest request,
                                                  @RequestParam Integer creatorUserId) {
        WorkOrderResponse response = workOrderService.create(request, creatorUserId);
        return ApiResponse.success(HttpStatus.OK.value(), "work order created", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrderResponse> getById(@PathVariable Integer id) {
        WorkOrderResponse response = workOrderService.getById(id);
        return ApiResponse.success(HttpStatus.OK.value(), "success", response);
    }

    @GetMapping
    public ApiResponse<Page<WorkOrderListItemResponse>> list(@RequestParam(required = false) Integer priorityId,
                                                               @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<WorkOrderListItemResponse> response = workOrderService.list(priorityId, pageable);
        return ApiResponse.success(HttpStatus.OK.value(), "success", response);
    }
}
