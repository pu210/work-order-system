package com.eeit219.work_order_system.modules.c.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.RejectWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.dto.ReviewAcceptRequest;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;

import jakarta.transaction.Transactional;

@Service
public class ReviewService {
    private final WorkOrderRepository workOrderRepository;
    private final PriorityRepository priorityRepository;
    private final UserRepository userRepository;
    private final WorkOrderStateMachineService workOrderStateMachineService;

    public ReviewService(WorkOrderRepository workOrderRepository,
            PriorityRepository priorityRepository,
            UserRepository userRepository,
            WorkOrderStateMachineService workOrderStateMachineService) {
        this.workOrderRepository = workOrderRepository;
        this.priorityRepository = priorityRepository;
        this.userRepository = userRepository;
        this.workOrderStateMachineService = workOrderStateMachineService;
    }

    @Transactional
    public void reviewAccept(ReviewAcceptRequest request, Integer workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));

        if (workOrder.getStatus() != WorkOrderState.PENDING_REVIEW) {
            throw new InvalidWorkOrderStateException("目前不是待審查狀態");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oldDueTime = workOrder.getDueTime();
        LocalDateTime newDueTime = request.dueTime();
        boolean dueTimeChanged = oldDueTime == null
                || !oldDueTime.isEqual(newDueTime);

        // 第一次設定期限，或管理員有修改期限時，
        // 新期限必須晚於現在。
        if (dueTimeChanged && !newDueTime.isAfter(now)) {
            throw new IllegalArgumentException(
                    "修改後的預計完成時間必須晚於現在");
        }

        workOrder = workOrderStateMachineService.changeState(workOrder, request.userId(), request.feedback(),
                WorkOrderEvent.ACCEPT);

        Priority priority = priorityRepository
                .findById(request.priorityId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到優先級"));

        User handler = userRepository
                .findById(request.assignedHandlerId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到處理人員"));
        workOrder.setPriority(priority);
        workOrder.setAssignedHandler(handler);
        workOrder.setDueTime(newDueTime);

        // 重新計算當下是否逾期
        boolean overdue = newDueTime.isBefore(now);
        workOrder.setIsOverdue(overdue);

        workOrderRepository.save(workOrder);
    }

    @Transactional
    public void reviewReject(RejectWorkOrderRequest request, Integer workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));

        if (workOrder.getStatus() != WorkOrderState.PENDING_REVIEW) {
            throw new InvalidWorkOrderStateException("目前不是待審查狀態");
        }

        workOrderStateMachineService.changeState(workOrder, request.userId(), request.feedback(),
                WorkOrderEvent.REJECT);
    }

}
