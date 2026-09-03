package com.eeit219.work_order_system.modules.c.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.entity.UserRole;
import com.eeit219.work_order_system.modules.a.repository.UserRoleRepository;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.ProgressAcceptRequest;
import com.eeit219.work_order_system.modules.c.dto.RejectWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.service.NotificationService;
import com.eeit219.work_order_system.modules.f.entity.RepairTarget;
import com.eeit219.work_order_system.modules.f.repository.RepairTargetRepository;

import jakarta.transaction.Transactional;

@Service
public class ProgressService {
        private final WorkOrderRepository workOrderRepository;
        private final WorkOrderStateMachineService workOrderStateMachineService;
        private final NotificationService notificationService;
        private final RepairTicketHistoryRepository repairTicketHistoryRepository;
        private final RepairTargetRepository repairTargetsRepository;
        private final UserRoleRepository userRoleRepository;

        public ProgressService(WorkOrderRepository workOrderRepository,
                        WorkOrderStateMachineService workOrderStateMachineService,
                        NotificationService notificationService,
                        RepairTicketHistoryRepository repairTicketHistoryRepository,
                        RepairTargetRepository repairTargetsRepository,
                        UserRoleRepository userRoleRepository) {
                this.workOrderRepository = workOrderRepository;
                this.workOrderStateMachineService = workOrderStateMachineService;
                this.notificationService = notificationService;
                this.repairTicketHistoryRepository = repairTicketHistoryRepository;
                this.repairTargetsRepository = repairTargetsRepository;
                this.userRoleRepository = userRoleRepository;
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
                RepairTarget repairTargets = repairTargetsRepository.findByTargetNo(request.targetNo())
                                .orElseThrow(() -> new ResourceNotFoundException("輸入的編號找不到對應的維修標的"));

                workOrder.setRepairTarget(repairTargets);
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

                // 2. 尋找接收退單通知的管理員 (多層級降級策略：1. workOrder.getAdmin() ➔ 2. 歷史紀錄 ➔ 3. 全體管理員廣播)
                Integer adminUserId = null;
                if (workOrder.getAdmin() != null) {
                        adminUserId = workOrder.getAdmin().getUserId();
                } else {
                        RepairTicketHistory reviewHistory = repairTicketHistoryRepository
                                        .findTopByWorkOrderWorkOrderIdAndEventOrderByHistoryIdDesc(workOrderId,
                                                        WorkOrderEvent.ACCEPT)
                                        .orElse(null);
                        if (reviewHistory != null && reviewHistory.getEditor() != null) {
                                adminUserId = reviewHistory.getEditor().getUserId();
                        }
                }

                String handlerName = workOrder.getAssignedHandler() != null ? workOrder.getAssignedHandler().getName()
                                : "工程師";
                String reasonStr = (request.feedback() != null && !request.feedback().isBlank()) ? request.feedback()
                                : "";
                String reasonPart = reasonStr.isBlank() ? " 已退回處理，請重新審核與指派。"
                                : " 已退回處理，退回原因：" + reasonStr + "，請重新審核與指派。";
                String title = "工程師已退回工單，待重新審核！";
                String message = "工單：" + workOrder.getWorkOrderNo() + "，處理人：" + handlerName + reasonPart;

                // 3. 發送退單通知
                if (adminUserId != null) {
                        notificationService.sendNotification(
                                        adminUserId, // 接收通知者：當初審核此工單的管理員 ID
                                        userId, // 發送通知者：退單工程師 ID
                                        workOrderId, // 工單 ID
                                        title, // 通知標題
                                        message, // 通知詳細內容
                                        workOrder.getStatus()); // 當前狀態 (PENDING_REVIEW)
                } else {
                        // 保底備用：如果沒有指定特定管理員，廣播發送給所有活躍管理員
                        List<Integer> adminUserIds = userRoleRepository.findUserIdsByRoleCodeAndStatus("ADMIN",
                                        User.UserStatus.ACTIVE);
                        for (Integer adminId : adminUserIds) {
                                notificationService.sendNotification(
                                                adminId,
                                                userId,
                                                workOrderId,
                                                title,
                                                message,
                                                workOrder.getStatus());
                        }
                }
        }
}
