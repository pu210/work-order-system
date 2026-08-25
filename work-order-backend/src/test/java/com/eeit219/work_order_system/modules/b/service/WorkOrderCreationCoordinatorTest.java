package com.eeit219.work_order_system.modules.b.service;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderCreationCoordinatorTest {

    @Mock
    private WorkOrderService workOrderService;

    @InjectMocks
    private WorkOrderCreationCoordinator coordinator;

    @Test
    void createWithRetry_callsCreateOnce_whenFirstAttemptSucceeds() {
        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        User creator = new User();
        WorkOrderResponse expected = WorkOrderResponse.builder().workOrderNo("WO-2026-0001").build();

        when(workOrderService.create(request, creator)).thenReturn(expected);

        WorkOrderResponse response = coordinator.createWithRetry(request, creator);

        assertSame(expected, response);
        verify(workOrderService, times(1)).create(request, creator);
    }

    @Test
    void createWithRetry_retriesUntilSuccess_afterCollisionFailures() {
        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        User creator = new User();
        WorkOrderResponse expected = WorkOrderResponse.builder().workOrderNo("WO-2026-0004").build();

        when(workOrderService.create(request, creator))
                .thenThrow(new DataIntegrityViolationException("撞號"))
                .thenThrow(new DataIntegrityViolationException("撞號"))
                .thenReturn(expected);

        WorkOrderResponse response = coordinator.createWithRetry(request, creator);

        assertSame(expected, response);
        verify(workOrderService, times(3)).create(request, creator);
    }

    @Test
    void createWithRetry_rethrowsLastFailure_afterExhaustingAllRetries() {
        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        User creator = new User();
        DataIntegrityViolationException firstFailure = new DataIntegrityViolationException("撞號1");
        DataIntegrityViolationException secondFailure = new DataIntegrityViolationException("撞號2");
        DataIntegrityViolationException thirdFailure = new DataIntegrityViolationException("撞號3");

        when(workOrderService.create(request, creator))
                .thenThrow(firstFailure)
                .thenThrow(secondFailure)
                .thenThrow(thirdFailure);

        DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class,
                () -> coordinator.createWithRetry(request, creator));

        assertSame(thirdFailure, thrown);
        verify(workOrderService, times(3)).create(request, creator);
    }
}
