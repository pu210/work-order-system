package com.eeit219.work_order_system.modules.b.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;

// 暫定
@Service
public class WorkOrderCreationCoordinator {

    private static final int MAX_RETRY = 3;

    private final WorkOrderService workOrderService;

    public WorkOrderCreationCoordinator(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    // 工單編號極端情況下可能併發撞號（UNIQUE 限制擋下），這裡負責重試
    public WorkOrderResponse createWithRetry(WorkOrderCreateRequest request, User creator) {
        DataIntegrityViolationException lastFailure = null;

        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                return workOrderService.create(request, creator);
            } catch (DataIntegrityViolationException e) {
                lastFailure = e;
            }
        }
        throw lastFailure;
    }
}
