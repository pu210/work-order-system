package com.eeit219.work_order_system.modules.d.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderFeedbackRecordResponse;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderFeedbackType;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderFeedbackRecordRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderFeedbackRecordService {

    private final WorkOrderDetailRepository workOrderDetailRepository;
    private final WorkOrderFeedbackRecordRepository feedbackRecordRepository;
    private final WorkOrderAuthorizationService workOrderAuthorizationService;

    /**
     * 回傳所有有填寫內容的接受流程回饋；完整紀錄僅限管理員查閱。
     */
    @Transactional(readOnly = true)
    public List<WorkOrderFeedbackRecordResponse> getAllFeedbackRecords(
            Integer workOrderId,
            User currentUser) {

        workOrderAuthorizationService.validateAuthenticated(currentUser);

        WorkOrder workOrder = workOrderDetailRepository
                .findDetailById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到報修單，ID：" + workOrderId));

        workOrderAuthorizationService.validateViewPermission(workOrder, currentUser);

        if (!workOrderAuthorizationService.isAdmin(currentUser)) {
            throw new AccessDeniedException("只有管理員可查看所有流程回饋");
        }

        return feedbackRecordRepository
                .findByWorkOrderWorkOrderIdAndEventOrderByEditedTimeDescHistoryIdDesc(
                        workOrderId,
                        WorkOrderEvent.ACCEPT)
                .stream()
                .filter(history -> StringUtils.hasText(history.getFeedback()))
                .map(this::toResponse)
                .toList();
    }

    private WorkOrderFeedbackRecordResponse toResponse(RepairTicketHistory history) {
        User editor = history.getEditor();

        return WorkOrderFeedbackRecordResponse.builder()
                .historyId(history.getHistoryId())
                .feedbackType(resolveFeedbackType(history.getStatus()))
                .submittedByUserId(editor == null ? null : editor.getUserId())
                .submittedByName(editor == null ? null : editor.getName())
                .feedback(history.getFeedback().trim())
                .submittedTime(history.getEditedTime())
                .resultingStatus(history.getStatus())
                .build();
    }

    private WorkOrderFeedbackType resolveFeedbackType(WorkOrderState resultingStatus) {
        return switch (resultingStatus) {
            case IN_PROGRESS -> WorkOrderFeedbackType.ADMIN_REVIEW;
            case PENDING_USER_ACCEPTANCE -> WorkOrderFeedbackType.HANDLER_COMPLETION;
            case PENDING_ADMIN_ACCEPTANCE -> WorkOrderFeedbackType.USER_ACCEPTANCE;
            case COMPLETED -> WorkOrderFeedbackType.ADMIN_ACCEPTANCE;
            default -> throw new IllegalStateException("無法辨識的流程回饋狀態：" + resultingStatus);
        };
    }
}
