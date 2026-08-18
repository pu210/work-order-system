package com.eeit219.work_order_system.modules.c.service;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.AcceptWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.dto.RejectWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import jakarta.transaction.Transactional;

@Service
public class ProgressService {
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStateMachineService workOrderStateMachineService;

    public ProgressService(WorkOrderRepository workOrderRepository,
            WorkOrderStateMachineService workOrderStateMachineService) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderStateMachineService = workOrderStateMachineService;
    }

    @Transactional
    public void progressAccept(AcceptWorkOrderRequest request, Integer workOrderId, Integer userId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));
        if (!workOrder.getAssignedHandler().getUserId().equals(userId)) {
            throw new AccessDeniedException("只有被指派的處理人可以操作此工單");
        }

        if (workOrder.getStatus() != WorkOrderState.IN_PROGRESS) {
            throw new InvalidWorkOrderStateException("目前不是進行中狀態");
        }

        workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                WorkOrderEvent.ACCEPT);
        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void progressReject(RejectWorkOrderRequest request, Integer workOrderId, Integer userId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));

        if (!workOrder.getAssignedHandler().getUserId().equals(userId)) {
            throw new AccessDeniedException("只有被指派的處理人可以操作此工單");
        }
        if (workOrder.getStatus() != WorkOrderState.IN_PROGRESS) {
            throw new InvalidWorkOrderStateException("目前不是進行中狀態");
        }

        workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                WorkOrderEvent.REJECT);
        workOrderRepository.save(workOrder);
    }

}
