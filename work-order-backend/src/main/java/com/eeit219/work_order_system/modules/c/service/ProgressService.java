package com.eeit219.work_order_system.modules.c.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.ProgressAcceptRequest;
import com.eeit219.work_order_system.modules.c.dto.RejectWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.service.NotificationService;
import com.eeit219.work_order_system.modules.f.entity.RepairTargets;
import com.eeit219.work_order_system.modules.f.repository.RepairTargetsRepository;

import jakarta.transaction.Transactional;

@Service
public class ProgressService {
        private final WorkOrderRepository workOrderRepository;
        private final WorkOrderStateMachineService workOrderStateMachineService;
        private final NotificationService notificationService;
        private final RepairTicketHistoryRepository repairTicketHistoryRepository;
        private final RepairTargetsRepository repairTargetsRepository;

        public ProgressService(WorkOrderRepository workOrderRepository,
                        WorkOrderStateMachineService workOrderStateMachineService,
                        NotificationService notificationService,
                        RepairTicketHistoryRepository repairTicketHistoryRepository,
                        RepairTargetsRepository repairTargetsRepository) {
                this.workOrderRepository = workOrderRepository;
                this.workOrderStateMachineService = workOrderStateMachineService;
                this.notificationService = notificationService;
                this.repairTicketHistoryRepository = repairTicketHistoryRepository;
                this.repairTargetsRepository = repairTargetsRepository;
        }

        // 工程師回報完成維修（進行中 IN_PROGRESS -> 待使用者驗收 PENDING_USER_ACCEPTANCE）
        @Transactional
        public void progressAccept(ProgressAcceptRequest request, Integer workOrderId, Integer userId) {
                WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));
                if (!workOrder.getAssignedHandler().getUserId().equals(userId)) {
                        throw new AccessDeniedException("只有被指派的處理人可以操作此工單");
                }

                if (workOrder.getStatus() != WorkOrderState.IN_PROGRESS) {
                        throw new InvalidWorkOrderStateException("目前不是進行中狀態");
                }
                // 查找維修標的
                RepairTargets repairTargets = repairTargetsRepository.findByTargetNo(request.targetNo())
                                .orElseThrow(() -> new ResourceNotFoundException("輸入的編號找不到對應的維修標的"));

                workOrder.setRepairTargets(repairTargets);
                // 1. 切換狀態機狀態 (IN_PROGRESS -> PENDING_USER_ACCEPTANCE)
                workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                                WorkOrderEvent.ACCEPT);
                workOrderRepository.save(workOrder);

                // 2. 傳送維修成功通知給「報修申請者」，提醒進行驗收
                notificationService.sendNotification(
                                workOrder.getCreator().getUserId(), // 接收通知者：報修人 ID
                                workOrder.getAssignedHandler().getUserId(), // 發送通知者：處理工程師 ID
                                workOrderId, // 工單 ID
                                "工單已維修完成，等待您的驗收！", // 通知標題
                                "工單：" + workOrder.getWorkOrderNo() + " 已由工程師 "
                                                + workOrder.getAssignedHandler().getName()
                                                + " 處理完成，請進行驗收。", // 通知詳細內容
                                workOrder.getStatus()); // 當前狀態 (PENDING_USER_ACCEPTANCE)
        }

        // 工程師退單（進行中 IN_PROGRESS -> 退回待審核 PENDING_REVIEW）
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

                // 1. 切換狀態機狀態 (IN_PROGRESS -> PENDING_REVIEW)
                workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                                WorkOrderEvent.REJECT);
                workOrderRepository.save(workOrder);

                // 2. 查詢歷史紀錄：找出「當初審核並接受這張工單」的那一位管理員
                RepairTicketHistory reviewHistory = repairTicketHistoryRepository
                                .findTopByWorkOrderWorkOrderIdAndEventOrderByHistoryIdDesc(workOrderId,
                                                WorkOrderEvent.ACCEPT)
                                .orElse(null);

                // 3. 如果有找到當初審核的管理員，發送退單通知給該位管理員
                if (reviewHistory != null && reviewHistory.getEditor() != null) {
                        Integer adminUserId = reviewHistory.getEditor().getUserId();

                        notificationService.sendNotification(
                                        adminUserId, // 接收通知者：當初審核此工單的管理員 ID
                                        userId, // 發送通知者：退單工程師 ID
                                        workOrderId, // 工單 ID
                                        "工程師已退回工單，待重新審核！", // 通知標題
                                        "工單：" + workOrder.getWorkOrderNo() + "，處理人："
                                                        + workOrder.getAssignedHandler().getName()
                                                        + " 已退回處理，退回原因：" + request.feedback() + "，請重新審核與指派。", // 通知詳細內容
                                        workOrder.getStatus()); // 當前狀態 (PENDING_REVIEW)
                }
        }
}
