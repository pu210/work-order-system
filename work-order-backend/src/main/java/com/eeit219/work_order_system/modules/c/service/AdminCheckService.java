package com.eeit219.work_order_system.modules.c.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.AdminCheckAcceptRequest;
import com.eeit219.work_order_system.modules.c.dto.RejectWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
public class AdminCheckService {
        private final WorkOrderRepository workOrderRepository;
        private final WorkOrderStateMachineService workOrderStateMachineService;
        private final NotificationService notificationService;
        private final RepairTicketHistoryRepository repairTicketHistoryRepository;

        public AdminCheckService(WorkOrderRepository workOrderRepository,
                        WorkOrderStateMachineService workOrderStateMachineService,
                        NotificationService notificationService,
                        RepairTicketHistoryRepository repairTicketHistoryRepository) {
                this.workOrderRepository = workOrderRepository;
                this.workOrderStateMachineService = workOrderStateMachineService;
                this.notificationService = notificationService;
                this.repairTicketHistoryRepository = repairTicketHistoryRepository;
        }

        @Transactional
        public void adminCheckAccept(AdminCheckAcceptRequest request, Integer workOrderId, Integer userId) {
                WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));

                if (workOrder.getStatus() != WorkOrderState.PENDING_ADMIN_ACCEPTANCE) {
                        throw new InvalidWorkOrderStateException("目前不是管理員審核狀態");
                }
                if (!workOrder.getAdmin().getUserId().equals(userId)) {
                        throw new AccessDeniedException("只有原審核管理員可以操作此工單");
                }

                workOrderStateMachineService.changeState(workOrder, userId, request.toFeedback(),
                                WorkOrderEvent.ACCEPT);
                workOrderRepository.save(workOrder);
        }

        @Transactional
        public void adminCheckReject(RejectWorkOrderRequest request, Integer workOrderId, Integer userId) {
                WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));

                if (workOrder.getStatus() != WorkOrderState.PENDING_ADMIN_ACCEPTANCE) {
                        throw new InvalidWorkOrderStateException("目前不是管理員審核狀態");
                }
                if (!workOrder.getAdmin().getUserId().equals(userId)) {
                        throw new AccessDeniedException("只有原審核管理員可以操作此工單");
                }
                workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                                WorkOrderEvent.REJECT);
                workOrderRepository.save(workOrder);
                // 2. 查詢歷史紀錄：找出「當初審核並接受這張工單」的那一位管理員
                RepairTicketHistory reviewHistory = repairTicketHistoryRepository
                                .findTopByWorkOrderWorkOrderIdAndEventOrderByHistoryIdDesc(workOrderId,
                                                WorkOrderEvent.ACCEPT)
                                .orElse(null);

                // 發送者 ID (保底機制)：優先使用「當初審核的管理員」，若查不到歷史紀錄，則改用「目前退單的管理員 (userId)」
                Integer senderAdminId = (reviewHistory != null && reviewHistory.getEditor() != null)
                                ? reviewHistory.getEditor().getUserId()
                                : userId;

                // 傳送退單通知給負責工程師（無論歷史紀錄是否存在，工程師都一定能收到通知）
                if (workOrder.getAssignedHandler() != null) {
                        String reasonStr = (request.feedback() != null && !request.feedback().isBlank()) ? request.feedback() : "";
                        String reasonPart = reasonStr.isBlank() ? "已被管理員退回驗收。" : "已被管理員退回驗收，原因：" + reasonStr;

                        notificationService.sendNotification(
                                        workOrder.getAssignedHandler().getUserId(), // 接收者：工程師
                                        senderAdminId, // 發送者：當初審核管理員 (或目前使用者)
                                        workOrderId,
                                        "管理員退回驗收，請重新處理！",
                                        "工單：" + workOrder.getWorkOrderNo() + " " + reasonPart,
                                        workOrder.getStatus());
                }

        }
}
