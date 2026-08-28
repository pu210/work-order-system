package com.eeit219.work_order_system.modules.c.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.AcceptWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
public class UserCheckService {
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStateMachineService workOrderStateMachineService;
    private final NotificationService notificationService;
    private final RepairTicketHistoryRepository repairTicketHistoryRepository;

    public UserCheckService(WorkOrderRepository workOrderRepository,
            WorkOrderStateMachineService workOrderStateMachineService,
            NotificationService notificationService,
            RepairTicketHistoryRepository repairTicketHistoryRepository) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderStateMachineService = workOrderStateMachineService;
        this.notificationService = notificationService;
        this.repairTicketHistoryRepository = repairTicketHistoryRepository;
    }

    @Transactional
    public void userCheckAccept(AcceptWorkOrderRequest request, Integer workOrderId, Integer userId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));
        if (!workOrder.getCreator().getUserId().equals(userId)) {
            throw new AccessDeniedException("只有報修人員可以操作此工單");
        }

        if (workOrder.getStatus() != WorkOrderState.PENDING_USER_ACCEPTANCE) {
            throw new InvalidWorkOrderStateException("目前不是使用者驗收狀態");
        }

        // 1. 找出當初審核此工單的管理員 ID (優先取 workOrder.getAdmin()，備用查歷史紀錄)
        Integer adminUserId = null;
        if (workOrder.getAdmin() != null) {
            adminUserId = workOrder.getAdmin().getUserId();
        } else {
            // 必須在 changeState 前查詢，否則隨後寫入的 User ACCEPT 紀錄會覆蓋真正的管理員審核紀錄
            RepairTicketHistory reviewHistory = repairTicketHistoryRepository
                    .findTopByWorkOrderWorkOrderIdAndEventOrderByHistoryIdDesc(workOrderId, WorkOrderEvent.ACCEPT)
                    .orElse(null);
            if (reviewHistory != null && reviewHistory.getEditor() != null) {
                adminUserId = reviewHistory.getEditor().getUserId();
            }
        }

        // 2. 執行狀態轉換 (PENDING_USER_ACCEPTANCE -> PENDING_ADMIN_ACCEPTANCE)
        workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                WorkOrderEvent.ACCEPT);
        workOrderRepository.save(workOrder);

        // 3. 如果有找到當初審核的管理員，發送驗收通知給該位管理員
        if (adminUserId != null) {
            String handlerName = workOrder.getAssignedHandler() != null ? workOrder.getAssignedHandler().getName() : "未指定";
            notificationService.sendNotification(
                    adminUserId, // 接收通知者：當初審核此工單的管理員 ID
                    userId, // 發送通知者：該工單建立者 ID (使用者)
                    workOrderId, // 工單 ID
                    "使用者已驗收回饋，請確認！", // 通知標題
                    "工單：" + workOrder.getWorkOrderNo() + "，處理人：" + handlerName
                            + " 已完成並得到使用者回饋：" + request.feedback() + "，請確認。", // 通知詳細內容
                    workOrder.getStatus()); // 當前狀態 (PENDING_ADMIN_ACCEPTANCE)
        }
    }
}
