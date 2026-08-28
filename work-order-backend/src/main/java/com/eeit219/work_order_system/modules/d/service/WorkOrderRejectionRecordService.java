package com.eeit219.work_order_system.modules.d.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderRejectionRecordResponse;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderRejectionType;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderRejectionRecordRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderRejectionRecordService {

    private final WorkOrderDetailRepository workOrderDetailRepository;
    private final WorkOrderRejectionRecordRepository rejectionRecordRepository;
    private final WorkOrderAuthorizationService workOrderAuthorizationService;

    /**
     * 查詢目前使用者有權查看的退回紀錄。
     *
     * 權限必須在後端過濾，避免未授權的退回原因出現在 API 回應中。
     */
    @Transactional(readOnly = true)
    public List<WorkOrderRejectionRecordResponse> getVisibleRejectionRecords(
            Integer workOrderId,
            User currentUser) {

        workOrderAuthorizationService.validateAuthenticated(currentUser);

        WorkOrder workOrder = workOrderDetailRepository
                .findDetailById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到報修單，ID：" + workOrderId));

        workOrderAuthorizationService.validateViewPermission(workOrder, currentUser);

        return rejectionRecordRepository
                .findByWorkOrderWorkOrderIdAndEventOrderByEditedTimeDescHistoryIdDesc(
                        workOrderId,
                        WorkOrderEvent.REJECT)
                .stream()
                .filter(history -> StringUtils.hasText(history.getFeedback()))
                .filter(history -> canViewRecord(history, workOrder, currentUser))
                .map(this::toResponse)
                .toList();
    }

    /**
     * 依退回後狀態套用角色可見規則。
     *
     * 管理員可看全部；建立人只看管理員初審拒絕；
     * 負責工程師只看管理員驗收退回重做。
     */
    private boolean canViewRecord(
            RepairTicketHistory history,
            WorkOrder workOrder,
            User currentUser) {

        if (workOrderAuthorizationService.isAdmin(currentUser)) {
            return true;
        }

        WorkOrderState resultingStatus = history.getStatus();

        if (resultingStatus == WorkOrderState.CANCELLED) {
            return workOrderAuthorizationService.isCreator(workOrder, currentUser);
        }

        if (resultingStatus == WorkOrderState.IN_PROGRESS) {
            return workOrderAuthorizationService.isAssignedHandler(workOrder, currentUser);
        }

        return false;
    }

    /**
     * 將歷程紀錄轉換為前端需要的退回紀錄 DTO。
     */
    private WorkOrderRejectionRecordResponse toResponse(RepairTicketHistory history) {
        User editor = history.getEditor();

        return WorkOrderRejectionRecordResponse.builder()
                .historyId(history.getHistoryId())
                .rejectionType(resolveRejectionType(history.getStatus()))
                .rejectedByUserId(editor == null ? null : editor.getUserId())
                .rejectedByName(editor == null ? null : editor.getName())
                .reason(history.getFeedback().trim())
                .rejectedTime(history.getEditedTime())
                .resultingStatus(history.getStatus())
                .build();
    }

    /**
     * 依退回後工單狀態辨識本次退回所屬流程。
     */
    private WorkOrderRejectionType resolveRejectionType(WorkOrderState resultingStatus) {
        return switch (resultingStatus) {
            case PENDING_REVIEW -> WorkOrderRejectionType.HANDLER_RETURNED;
            case CANCELLED -> WorkOrderRejectionType.ADMIN_REJECTED;
            case IN_PROGRESS -> WorkOrderRejectionType.ADMIN_RETURNED_FOR_REWORK;
            default -> throw new IllegalStateException("無法辨識的工單退回狀態：" + resultingStatus);
        };
    }
}
