package com.eeit219.work_order_system.modules.c.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.c.dto.ChangeWorkOrderStateRequest;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import reactor.core.publisher.Mono;

@Service
public class WorkOrderStateMachineService {
    private final WorkOrderRepository workOrderRepository;
    private final RepairTicketHistoryRepository repairTicketHistoryRepository;
    private final StateMachineFactory<WorkOrderState, WorkOrderEvent> stateMachineFactory;

    public WorkOrderStateMachineService(
            WorkOrderRepository workOrderRepository,
            RepairTicketHistoryRepository repairTicketHistoryRepository,
            StateMachineFactory<WorkOrderState, WorkOrderEvent> stateMachineFactory) {
        this.workOrderRepository = workOrderRepository;
        this.repairTicketHistoryRepository = repairTicketHistoryRepository;
        this.stateMachineFactory = stateMachineFactory;
    }

    @Transactional
    public void review(ChangeWorkOrderStateRequest request) {

        // 管理員審查需要的處理
        WorkOrder workOrder = new WorkOrder();
        workOrder = findByWorkOrderId(request.workOrderId());
        if (workOrder.getStatus() != WorkOrderState.PENDING_REVIEW) {
            throw new IllegalStateException("目前不是待審查狀態");
        }
        if (request.event() == WorkOrderEvent.ACCEPT) {
            workOrder = changeState(workOrder, request.userId(), request.event());
            workOrder.setPriorityId(request.priorityId());
            workOrder.setDueTime(request.dueTime());
            workOrder.setAssignedHandler(request.assignedHandler());
            workOrderRepository.save(workOrder);
        }else{
            changeState(workOrder, request.userId(), request.event());
        }
    }

    // @Transactional
    // public WorkOrder evaluate(...) {
    // // 工單評估需要的處理
    // return changeState(workOrderId, userId, event);
    // }

    // @Transactional
    // public WorkOrder completeRepair() {
    // // 完工需要的處理
    // return changeState(workOrderId, userId, event);
    // }

    // @Transactional
    // public WorkOrder userAcceptance(...) {
    // // 使用者驗收需要的處理
    // return changeState(workOrderId, userId, event);
    // }

    // @Transactional
    // public WorkOrder adminAcceptance(...) {
    // // 管理員驗收需要的處理
    // return changeState(workOrderId, userId, event);
    // }

    private WorkOrder changeState(
            WorkOrder workOrder,
            Integer userId,
            WorkOrderEvent event) {

        // 取得資料庫目前狀態
        WorkOrderState oldState = workOrder.getStatus();

        // 建立這張工單的狀態機
        StateMachine<WorkOrderState, WorkOrderEvent> stateMachine = stateMachineFactory.getStateMachine(
                workOrder.getWorkOrderId().toString());

        try {
            // 將狀態機設定成資料庫目前狀態
            resetStateMachine(stateMachine, oldState);

            // 發送事件
            boolean accepted = sendEvent(stateMachine, event);

            // 檢查事件能不能執行如果不能執行就丟出例外
            if (!accepted) {
                throw new IllegalStateException(
                        "狀態 " + oldState +
                                " 不允許執行 " + event);
            }

            // 取得切換後的狀態
            WorkOrderState newState = stateMachine.getState().getId();

            // 改變資料庫status狀態
            modifyWorkOrderStatus(workOrder, newState);

            // 創建一筆新的history
            createRepairTicketHistory(workOrder, userId);
            return workOrder;

        } finally {
            stateMachine.stopReactively().block();
        }

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

    private void resetStateMachine(
            StateMachine<WorkOrderState, WorkOrderEvent> stateMachine,
            WorkOrderState currentState) {

        // 先停止
        stateMachine.stopReactively().block();

        // 設定成資料庫目前狀態
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> access.resetStateMachineReactively(
                        new DefaultStateMachineContext<>(
                                currentState,
                                null,
                                null,
                                null))
                        .block());

        // 重新啟動
        stateMachine.startReactively().block();
    }

    private boolean sendEvent(
            StateMachine<WorkOrderState, WorkOrderEvent> stateMachine,
            WorkOrderEvent event) {

        Message<WorkOrderEvent> message = new GenericMessage<>(event);

        StateMachineEventResult<WorkOrderState, WorkOrderEvent> result = stateMachine.sendEvent(Mono.just(message))
                .blockFirst();

        // 沒有結果，代表事件沒有執行
        if (result == null) {
            return false;
        }

        // 等待狀態切換完成
        result.complete().block();

        // 回傳是否接受事件
        return result.getResultType() == StateMachineEventResult.ResultType.ACCEPTED;
    }

    public WorkOrder modifyWorkOrderStatus(WorkOrder workOrder, WorkOrderState newState) {
        workOrder.setStatus(newState);
        return workOrderRepository.save(workOrder);
    }

    public RepairTicketHistory createRepairTicketHistory(WorkOrder workOrder, Integer editorId) {
        RepairTicketHistory history = new RepairTicketHistory();
        history.setTicketId(workOrder.getWorkOrderId());
        history.setStatus(workOrder.getStatus());
        history.setEditedTime(LocalDateTime.now());
        history.setEditorId(editorId);
        return repairTicketHistoryRepository.save(history);
    }
}