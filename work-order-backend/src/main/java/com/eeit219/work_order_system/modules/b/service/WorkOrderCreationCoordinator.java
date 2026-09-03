package com.eeit219.work_order_system.modules.b.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;

@Service
public class WorkOrderCreationCoordinator {

    private static final int MAX_RETRY = 3;

    private final WorkOrderService workOrderService;

    public WorkOrderCreationCoordinator(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    // 工單編號極端情況下可能併發撞號（UNIQUE 限制擋下），這裡負責重試。
    // 附件驗證失敗（IllegalArgumentException）不屬於撞號情境，不會被這裡攔截，直接往外拋、不重試。
    public WorkOrderResponse createWithRetry(WorkOrderCreateRequest request, User creator,
            List<MultipartFile> files) {
        DataIntegrityViolationException lastFailure = null;

        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                return workOrderService.create(request, creator, files);
            } catch (DataIntegrityViolationException e) {
                lastFailure = e;
            }
        }
        throw lastFailure;
    }
}
