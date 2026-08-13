package com.eeit219.work_order_system.modules.d.repository;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderDetailRepositoryTest {

    @Mock
    private WorkOrderDetailRepository workOrderDetailRepository;

    @Test
    void findDetailById_returnsWorkOrder_whenWorkOrderExists() {
        Integer workOrderId = 1;
        WorkOrder workOrder = new WorkOrder();
        when(workOrderDetailRepository.findDetailById(workOrderId))
                .thenReturn(Optional.of(workOrder));

        Optional<WorkOrder> result = workOrderDetailRepository.findDetailById(workOrderId);

        assertTrue(result.isPresent());
        assertSame(workOrder, result.get());
        verify(workOrderDetailRepository).findDetailById(workOrderId);
    }

    @Test
    void findDetailById_returnsEmpty_whenWorkOrderDoesNotExist() {
        Integer workOrderId = 999;
        when(workOrderDetailRepository.findDetailById(workOrderId))
                .thenReturn(Optional.empty());

        Optional<WorkOrder> result = workOrderDetailRepository.findDetailById(workOrderId);

        assertTrue(result.isEmpty());
        verify(workOrderDetailRepository).findDetailById(workOrderId);
    }
}
