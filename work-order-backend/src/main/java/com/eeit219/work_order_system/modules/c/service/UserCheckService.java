package com.eeit219.work_order_system.modules.c.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.c.dto.ChangeStatusRequest;
import com.eeit219.work_order_system.modules.c.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import jakarta.transaction.Transactional;

@Service
public class UserCheckService {
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStateMachineService workOrderStateMachineService;

    public UserCheckService (WorkOrderRepository workOrderRepository,WorkOrderStateMachineService workOrderStateMachineService) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderStateMachineService=workOrderStateMachineService;
    }

    @Transactional
    public void userCheckAccept(ChangeStatusRequest request, Integer workOrderId) {
        WorkOrder workOrder = findByWorkOrderId(workOrderId);

        if (workOrder == null) {
            throw new IllegalArgumentException("找不到指定的工單");
        }

        if (workOrder.getStatus() != WorkOrderState.PENDING_USER_ACCEPTANCE) {
            throw new IllegalStateException("目前不是使用者驗收狀態");
        }

        workOrderStateMachineService.changeState(workOrder, request.userId(), request.feedback(), WorkOrderEvent.ACCEPT);
    }
    public WorkOrder findByWorkOrderId(Integer id) {
        if (id != null) {
            Optional<WorkOrder> optional = workOrderRepository.findById(id);
            if (optional != null && optional.isPresent()) {
                return optional.get();
            }
        }
        return null;
    }

}
