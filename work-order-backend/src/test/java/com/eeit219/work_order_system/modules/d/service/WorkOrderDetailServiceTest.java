package com.eeit219.work_order_system.modules.d.service;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderDetailResponse;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderDetailServiceTest {

    @Mock
    private WorkOrderDetailRepository workOrderDetailRepository;

    @Mock
    private WorkOrderAuthorizationService workOrderAuthorizationService;

    @InjectMocks
    private WorkOrderDetailService workOrderDetailService;

    @Test
    void getWorkOrderDetail_throwsAccessDenied_whenCurrentUserIsNull() {
        doThrow(new AccessDeniedException("使用者尚未登入"))
                .when(workOrderAuthorizationService)
                .validateAuthenticated(null);

        assertThrows(
                AccessDeniedException.class,
                () -> workOrderDetailService.getWorkOrderDetail(1, null)
        );

        verify(workOrderAuthorizationService).validateAuthenticated(null);
        verifyNoInteractions(workOrderDetailRepository);
    }

    @Test
    void getWorkOrderDetail_throwsEntityNotFound_whenWorkOrderDoesNotExist() {
        User currentUser = mock(User.class);
        when(workOrderDetailRepository.findDetailById(999)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> workOrderDetailService.getWorkOrderDetail(999, currentUser)
        );

        verify(workOrderAuthorizationService).validateAuthenticated(currentUser);
        verifyNoMoreInteractions(workOrderAuthorizationService);
        verify(workOrderDetailRepository).findDetailById(999);
    }

    @Test
    void getWorkOrderDetail_returnsDetail_whenAuthorized() {
        Integer workOrderId = 1;
        User currentUser = mock(User.class);
        WorkOrder workOrder = mock(WorkOrder.class);

        when(workOrder.getWorkOrderId()).thenReturn(workOrderId);
        when(workOrder.getWorkOrderNo()).thenReturn("WO-2026-0001");
        when(workOrderDetailRepository.findDetailById(workOrderId))
                .thenReturn(Optional.of(workOrder));

        WorkOrderDetailResponse result =
                workOrderDetailService.getWorkOrderDetail(workOrderId, currentUser);

        assertEquals(workOrderId, result.getWorkOrderId());
        assertEquals("WO-2026-0001", result.getWorkOrderNo());
        verify(workOrderAuthorizationService).validateAuthenticated(currentUser);
        verify(workOrderAuthorizationService).validateViewPermission(workOrder, currentUser);
        verifyNoMoreInteractions(workOrderAuthorizationService);
        verify(workOrderDetailRepository).findDetailById(workOrderId);
    }
}
