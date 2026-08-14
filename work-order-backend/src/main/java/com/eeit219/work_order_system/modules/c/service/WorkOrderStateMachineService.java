package com.eeit219.work_order_system.modules.c.service;

import java.time.LocalDateTime;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.c.repository.UserRepositoryC;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import reactor.core.publisher.Mono;

@Service
public class WorkOrderStateMachineService {
    private final RepairTicketHistoryRepository repairTicketHistoryRepository;
    private final UserRepositoryC userRepository;
    private final StateMachineFactory<WorkOrderState, WorkOrderEvent> stateMachineFactory;

    public WorkOrderStateMachineService(
            RepairTicketHistoryRepository repairTicketHistoryRepository,
            UserRepositoryC userRepository,
            StateMachineFactory<WorkOrderState, WorkOrderEvent> stateMachineFactory) {
        this.userRepository = userRepository;
        this.repairTicketHistoryRepository = repairTicketHistoryRepository;
        this.stateMachineFactory = stateMachineFactory;
    }

    public WorkOrder changeState(
            WorkOrder workOrder,
            Integer userId,
            String feedback,
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
                throw new InvalidWorkOrderStateException(
                        "狀態 " + oldState +
                                " 不允許執行 " + event);
            }

            // 取得切換後的狀態
            WorkOrderState newState = stateMachine.getState().getId();

            // 改變資料庫status狀態
            workOrder.setStatus(newState);

            // 創建一筆新的history
            createRepairTicketHistory(workOrder, userId, feedback, event);
            // 發送通知

            return workOrder;

        } finally {
            stateMachine.stopReactively().block();
        }

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
    private RepairTicketHistory createRepairTicketHistory(WorkOrder workOrder, Integer editorId, String feedback,
            WorkOrderEvent event) {
        RepairTicketHistory history = new RepairTicketHistory();
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到編輯人"));
        history.setWorkOrder(workOrder);
        history.setStatus(workOrder.getStatus());
        history.setEditedTime(LocalDateTime.now());
        history.setEditor(editor);
        history.setFeedback(feedback);
        history.setEvent(event);
        return repairTicketHistoryRepository.save(history);
    }

}