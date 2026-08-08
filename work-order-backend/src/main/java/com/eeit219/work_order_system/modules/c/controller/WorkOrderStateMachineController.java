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

    @PostMapping("/state")
    public Map<String, Object> changeState(
            @RequestBody ChangeWorkOrderStateRequest request) {

        try {
            stateMachineService.changeState(
                    request.workOrderId(),
                    request.event());

            return Map.of(
                    "success", true,
                    "message", "成功");

        } catch (IllegalStateException exception) {
            return Map.of(
                    "success", false,
                    "message", "無法執行該動作");
        }
    }
}