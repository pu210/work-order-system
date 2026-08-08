package com.eeit219.work_order_system.modules.c.service;

import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.c.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import reactor.core.publisher.Mono;

@Service
public class WorkOrderStateMachineService {
    private final WorkOrderRepository workOrderRepository;
    private final StateMachineFactory<WorkOrderState, WorkOrderEvent> stateMachineFactory;

    public WorkOrderStateMachineService(
            WorkOrderRepository workOrderRepository,
            StateMachineFactory<WorkOrderState, WorkOrderEvent> stateMachineFactory) {
        this.workOrderRepository = workOrderRepository;
        this.stateMachineFactory = stateMachineFactory;
    }

    @Transactional
    public WorkOrder changeState(
            Integer workOrderId,
            WorkOrderEvent event) {

        // 1. 查詢工單
        WorkOrder workOrder = findByWorkOrderId(workOrderId);

        // 2. 取得資料庫目前狀態
        WorkOrderState oldState = workOrder.getStatus();

        // 3. 建立這張工單的狀態機
        StateMachine<WorkOrderState, WorkOrderEvent> stateMachine = stateMachineFactory.getStateMachine(
                workOrderId.toString());

        try {
            // 4. 將狀態機設定成資料庫目前狀態
            resetStateMachine(stateMachine, oldState);

            // 5. 發送事件
            boolean accepted = sendEvent(stateMachine, event);

            // 6. 檢查事件能不能執行
            if (!accepted) {
                throw new IllegalStateException(
                        "狀態 " + oldState +
                                " 不允許執行 " + event);
            }

            // 7. 取得切換後的狀態
            WorkOrderState newState = stateMachine.getState().getId();

            // 8. 寫回資料庫
            workOrder.setStatus(newState);

            return workOrderRepository.save(workOrder);

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
}