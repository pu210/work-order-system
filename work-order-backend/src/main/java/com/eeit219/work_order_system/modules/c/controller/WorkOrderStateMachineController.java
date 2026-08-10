package com.eeit219.work_order_system.modules.c.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.modules.c.dto.ChangeWorkOrderStateRequest;
import com.eeit219.work_order_system.modules.c.service.WorkOrderStateMachineService;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderStateMachineController {

    private final WorkOrderStateMachineService stateMachineService;

    public WorkOrderStateMachineController(WorkOrderStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @PostMapping("/status")
    public Map<String, Object> PendingReviewChangeState(@RequestBody ChangeWorkOrderStateRequest request) {
        if (request.workOrderId() == null) {
            return Map.of("message", "工單ID不可為空");
        } else if (request.userId() == null) {
            return Map.of("message", "使用者ID不可為空");
        } else if (request.event() == null) {
            return Map.of("message", "事件不可為空");
        } else if (request.assignedHandler() == null) {
            return Map.of("message", "指派工程師不可為空");
        } else if (request.priorityId() == null) {
            return Map.of("message", "優先權不可為空");
        } else if (request.dueTime() == null) {
            return Map.of("message", "預計完成時間不可為空");
        } else if (request.event() == null) {
            return Map.of("message", "事件不可為空");
        } else {
            try {
                stateMachineService.review(request);
                return Map.of("message", "success");
            } catch (Exception e) {
                return Map.of("message", e.getMessage());
            }
        }
    }

}