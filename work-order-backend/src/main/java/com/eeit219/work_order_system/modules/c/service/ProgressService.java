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
public class ProgressService {
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStateMachineService workOrderStateMachineService;

    public ProgressService(WorkOrderRepository workOrderRepository,WorkOrderStateMachineService workOrderStateMachineService) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderStateMachineService=workOrderStateMachineService;
    }

    @Transactional
    public void progressAccept(ChangeStatusRequest request, Integer workOrderId) {
        WorkOrder workOrder = findByWorkOrderId(workOrderId);

        if (workOrder == null) {
            throw new IllegalArgumentException("找不到指定的工單");
        }

        if (workOrder.getStatus() != WorkOrderState.IN_PROGRESS) {
            throw new IllegalStateException("目前不是進行中狀態");
        }

        workOrderStateMachineService.changeState(workOrder, request.userId(), request.feedback(), WorkOrderEvent.ACCEPT);
    }

    @Transactional
    public void progressReject(ChangeStatusRequest request, Integer workOrderId) {
        WorkOrder workOrder = findByWorkOrderId(workOrderId);

        if (workOrder == null) {
            throw new IllegalArgumentException("找不到指定的工單");
        }

        if (workOrder.getStatus() != WorkOrderState.IN_PROGRESS) {
            throw new IllegalStateException("目前不是進行中狀態");
        }

        workOrderStateMachineService.changeState(workOrder, request.userId(), request.feedback(), WorkOrderEvent.REJECT);
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
