package com.eeit219.work_order_system.modules.d.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
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

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderRejectionRecordResponse;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderRejectionType;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderRejectionRecordRepository;

@ExtendWith(MockitoExtension.class)
class WorkOrderRejectionRecordServiceTest {

    @Mock
    private WorkOrderDetailRepository workOrderDetailRepository;

    @Mock
    private WorkOrderRejectionRecordRepository rejectionRecordRepository;

    @Mock
    private WorkOrderAuthorizationService workOrderAuthorizationService;

    @InjectMocks
    private WorkOrderRejectionRecordService rejectionRecordService;

    private final Integer workOrderId = 1;
    private WorkOrder workOrder;
    private User currentUser;
    private List<RepairTicketHistory> rejectionHistories;

    @BeforeEach
    void setUp() {
        workOrder = new WorkOrder();
        workOrder.setWorkOrderId(workOrderId);

        currentUser = new User();
        currentUser.setUserId(10);

        rejectionHistories = List.of(
                history(3, WorkOrderState.IN_PROGRESS, "請重新檢查設備"),
                history(2, WorkOrderState.CANCELLED, "不符合報修範圍"),
                history(1, WorkOrderState.PENDING_REVIEW, "目前無法處理"));

        when(workOrderDetailRepository.findDetailById(workOrderId))
                .thenReturn(Optional.of(workOrder));
        when(rejectionRecordRepository
                .findByWorkOrderWorkOrderIdAndEventOrderByEditedTimeDescHistoryIdDesc(
                        workOrderId,
                        WorkOrderEvent.REJECT))
                .thenReturn(rejectionHistories);
    }

    @Test
    void getVisibleRejectionRecords_returnsAllRecordsForAdmin() {
        when(workOrderAuthorizationService.isAdmin(currentUser)).thenReturn(true);

        List<WorkOrderRejectionRecordResponse> result =
                rejectionRecordService.getVisibleRejectionRecords(workOrderId, currentUser);

        assertEquals(3, result.size());
        assertEquals(
                WorkOrderRejectionType.ADMIN_RETURNED_FOR_REWORK,
                result.get(0).getRejectionType());
        assertEquals(WorkOrderRejectionType.ADMIN_REJECTED, result.get(1).getRejectionType());
        assertEquals(WorkOrderRejectionType.HANDLER_RETURNED, result.get(2).getRejectionType());
    }

    @Test
    void getVisibleRejectionRecords_returnsInitialAdminRejectionForCreator() {
        when(workOrderAuthorizationService.isAdmin(currentUser)).thenReturn(false);
        when(workOrderAuthorizationService.isCreator(workOrder, currentUser)).thenReturn(true);
        when(workOrderAuthorizationService.isAssignedHandler(workOrder, currentUser)).thenReturn(false);

        List<WorkOrderRejectionRecordResponse> result =
                rejectionRecordService.getVisibleRejectionRecords(workOrderId, currentUser);

        assertEquals(1, result.size());
        assertEquals(WorkOrderRejectionType.ADMIN_REJECTED, result.get(0).getRejectionType());
    }

    @Test
    void getVisibleRejectionRecords_returnsAdminReworkReasonForAssignedHandler() {
        when(workOrderAuthorizationService.isAdmin(currentUser)).thenReturn(false);
        when(workOrderAuthorizationService.isCreator(workOrder, currentUser)).thenReturn(false);
        when(workOrderAuthorizationService.isAssignedHandler(workOrder, currentUser)).thenReturn(true);

        List<WorkOrderRejectionRecordResponse> result =
                rejectionRecordService.getVisibleRejectionRecords(workOrderId, currentUser);

        assertEquals(1, result.size());
        assertEquals(
                WorkOrderRejectionType.ADMIN_RETURNED_FOR_REWORK,
                result.get(0).getRejectionType());
    }

    @Test
    void getVisibleRejectionRecords_returnsNoReasonsForOtherViewer() {
        when(workOrderAuthorizationService.isAdmin(currentUser)).thenReturn(false);
        when(workOrderAuthorizationService.isCreator(workOrder, currentUser)).thenReturn(false);
        when(workOrderAuthorizationService.isAssignedHandler(workOrder, currentUser)).thenReturn(false);

        List<WorkOrderRejectionRecordResponse> result =
                rejectionRecordService.getVisibleRejectionRecords(workOrderId, currentUser);

        assertEquals(0, result.size());
        verify(workOrderAuthorizationService).validateViewPermission(workOrder, currentUser);
    }

    /**
     * 建立測試用的 REJECT 歷程，模擬不同流程完成後的工單狀態。
     */
    private RepairTicketHistory history(
            Integer historyId,
            WorkOrderState status,
            String reason) {

        User editor = new User();
        editor.setUserId(historyId + 100);
        editor.setName("測試操作人員 " + historyId);

        RepairTicketHistory history = new RepairTicketHistory();
        history.setHistoryId(historyId);
        history.setWorkOrder(workOrder);
        history.setStatus(status);
        history.setEvent(WorkOrderEvent.REJECT);
        history.setFeedback(reason);
        history.setEditedTime(LocalDateTime.of(2026, 8, 27, 10, historyId));
        history.setEditor(editor);
        return history;
    }
}
