package com.eeit219.work_order_system.modules.d.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderFeedbackRecordResponse;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderFeedbackType;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderFeedbackRecordRepository;

@ExtendWith(MockitoExtension.class)
class WorkOrderFeedbackRecordServiceTest {

    @Mock
    private WorkOrderDetailRepository workOrderDetailRepository;

    @Mock
    private WorkOrderFeedbackRecordRepository feedbackRecordRepository;

    @Mock
    private WorkOrderAuthorizationService workOrderAuthorizationService;

    @InjectMocks
    private WorkOrderFeedbackRecordService feedbackRecordService;

    private final Integer workOrderId = 1;
    private WorkOrder workOrder;
    private User currentUser;

    @BeforeEach
    void setUp() {
        workOrder = new WorkOrder();
        workOrder.setWorkOrderId(workOrderId);

        currentUser = new User();
        currentUser.setUserId(10);

        when(workOrderDetailRepository.findDetailById(workOrderId))
                .thenReturn(Optional.of(workOrder));
    }

    @Test
    void getAllFeedbackRecords_returnsAllNonBlankAcceptFeedbackForAdmin() {
        when(workOrderAuthorizationService.isAdmin(currentUser)).thenReturn(true);
        when(feedbackRecordRepository
                .findByWorkOrderWorkOrderIdAndEventOrderByEditedTimeDescHistoryIdDesc(
                        workOrderId,
                        WorkOrderEvent.ACCEPT))
                .thenReturn(List.of(
                        history(4, WorkOrderState.COMPLETED, "驗收完成"),
                        history(3, WorkOrderState.PENDING_ADMIN_ACCEPTANCE, "設備正常"),
                        history(2, WorkOrderState.PENDING_USER_ACCEPTANCE, "已更換零件"),
                        history(1, WorkOrderState.IN_PROGRESS, "請進行維修"),
                        history(5, WorkOrderState.COMPLETED, " ")));

        List<WorkOrderFeedbackRecordResponse> result =
                feedbackRecordService.getAllFeedbackRecords(workOrderId, currentUser);

        assertEquals(4, result.size());
        assertEquals(WorkOrderFeedbackType.ADMIN_ACCEPTANCE, result.get(0).getFeedbackType());
        assertEquals(WorkOrderFeedbackType.USER_ACCEPTANCE, result.get(1).getFeedbackType());
        assertEquals(WorkOrderFeedbackType.HANDLER_COMPLETION, result.get(2).getFeedbackType());
        assertEquals(WorkOrderFeedbackType.ADMIN_REVIEW, result.get(3).getFeedbackType());
    }

    @Test
    void getAllFeedbackRecords_rejectsNonAdminBeforeReadingHistory() {
        when(workOrderAuthorizationService.isAdmin(currentUser)).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> feedbackRecordService.getAllFeedbackRecords(workOrderId, currentUser));

        verifyNoInteractions(feedbackRecordRepository);
    }

    private RepairTicketHistory history(
            Integer historyId,
            WorkOrderState status,
            String feedback) {

        User editor = new User();
        editor.setUserId(historyId + 100);
        editor.setName("測試操作人員 " + historyId);

        RepairTicketHistory history = new RepairTicketHistory();
        history.setHistoryId(historyId);
        history.setWorkOrder(workOrder);
        history.setStatus(status);
        history.setEvent(WorkOrderEvent.ACCEPT);
        history.setFeedback(feedback);
        history.setEditedTime(LocalDateTime.of(2026, 8, 28, 10, historyId));
        history.setEditor(editor);
        return history;
    }
}
