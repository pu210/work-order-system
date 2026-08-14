package com.eeit219.work_order_system.modules.c.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.AcceptWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import jakarta.transaction.Transactional;

@Service
public class UserCheckService {
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStateMachineService workOrderStateMachineService;

    public UserCheckService(WorkOrderRepository workOrderRepository,
            WorkOrderStateMachineService workOrderStateMachineService) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderStateMachineService = workOrderStateMachineService;
    }

    @Transactional
    public void userCheckAccept(AcceptWorkOrderRequest request, Integer workOrderId, Integer userId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));
        if (!workOrder.getCreator().getUserId().equals(userId)) {
            throw new AccessDeniedException("只有報修人員可以操作此工單");
        }

        if (workOrder.getStatus() != WorkOrderState.PENDING_USER_ACCEPTANCE) {
            throw new InvalidWorkOrderStateException("目前不是使用者驗收狀態");
        }

        workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                WorkOrderEvent.ACCEPT);
        workOrderRepository.save(workOrder);
    }

}