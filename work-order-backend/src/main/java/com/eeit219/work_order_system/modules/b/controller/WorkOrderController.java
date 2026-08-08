package com.eeit219.work_order_system.modules.b.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;
import com.eeit219.work_order_system.modules.b.service.WorkOrderService;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    public WorkOrderResponse create(@RequestBody WorkOrderCreateRequest request,
                                     @RequestParam Integer creatorUserId) {
        return workOrderService.create(request, creatorUserId);
    }

    @GetMapping("/{id}")
    public WorkOrderResponse getById(@PathVariable Integer id) {
        return workOrderService.getById(id);
    }
}
