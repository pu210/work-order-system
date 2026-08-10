package com.eeit219.work_order_system.modules.c.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.c.dto.ReviewAcceptRequest;
import com.eeit219.work_order_system.modules.c.dto.ReviewRejectRequest;
import com.eeit219.work_order_system.modules.c.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import jakarta.transaction.Transactional;

@Service
public class ReviewService {
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStateMachineService workOrderStateMachineService;

    public ReviewService(WorkOrderRepository workOrderRepository,WorkOrderStateMachineService workOrderStateMachineService) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderStateMachineService=workOrderStateMachineService;
    }

    @Transactional
    public void reviewAccept(ReviewAcceptRequest request, Integer workOrderId) {
        WorkOrder workOrder = findByWorkOrderId(workOrderId);

        if (workOrder == null) {
            throw new IllegalArgumentException("找不到指定的工單");
        }

        if (workOrder.getStatus() != WorkOrderState.PENDING_REVIEW) {
            throw new IllegalStateException("目前不是待審查狀態");
        }

        workOrder = workOrderStateMachineService.changeState(workOrder, request.userId(), request.feedback(), WorkOrderEvent.ACCEPT);
        workOrder.setPriorityId(request.priorityId());
        workOrder.setAssignedHandler(request.assignedHandler());
        workOrder.setDueTime(request.dueTime());

        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void reviewReject(ReviewRejectRequest request, Integer workOrderId) {
        WorkOrder workOrder = findByWorkOrderId(workOrderId);

        if (workOrder == null) {
            throw new IllegalArgumentException("找不到指定的工單");
        }

        if (workOrder.getStatus() != WorkOrderState.PENDING_REVIEW) {
            throw new IllegalStateException("目前不是待審查狀態");
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
