package com.eeit219.work_order_system.modules.b.service;

import java.time.LocalDateTime;
import java.time.Year;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;
import com.eeit219.work_order_system.modules.f.repository.SubCategoryRepository;

@Service
public class WorkOrderService {

    private static final Integer DEFAULT_PRIORITY_ID = 1;

    private final WorkOrderRepository workOrderRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final PriorityRepository priorityRepository;
    private final UserRepository userRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
            SubCategoryRepository subCategoryRepository,
            PriorityRepository priorityRepository,
            UserRepository userRepository) {
        this.workOrderRepository = workOrderRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.priorityRepository = priorityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request, Integer creatorUserId) {
        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("找不到子類別：" + request.getSubCategoryId()));

        Priority priority = resolvePriority(subCategory);

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalArgumentException("找不到使用者：" + creatorUserId));

        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNo(generateWorkOrderNo());
        workOrder.setTitle(request.getTitle());
        workOrder.setSubCategory(subCategory);
        workOrder.setPriority(priority);
        workOrder.setLocationDetail(request.getLocationDetail());
        workOrder.setContactPhone(request.getContactPhone());
        workOrder.setDescription(request.getDescription());
        workOrder.setCreator(creator);
        workOrder.setDueTime(null);
        workOrder.setCreatedTime(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(workOrder);

        return toResponse(saved, subCategory, priority, creator);
    }

    private Priority resolvePriority(SubCategory subCategory) {
        if (subCategory.getOverridePriority() != null) {
            return subCategory.getOverridePriority();
        }
        return priorityRepository.findById(DEFAULT_PRIORITY_ID)
                .orElseThrow(() -> new IllegalStateException("找不到預設優先級：" + DEFAULT_PRIORITY_ID));
    }

    private String generateWorkOrderNo() {
        String prefix = "WO-" + Year.now().getValue() + "-";

        int nextSequence = workOrderRepository.findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(prefix)
                .map(latest -> {
                    String sequencePart = latest.getWorkOrderNo().substring(latest.getWorkOrderNo().length() - 4);
                    return Integer.parseInt(sequencePart) + 1;
                })
                .orElse(1);

        return prefix + String.format("%04d", nextSequence);
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder, SubCategory subCategory,
            Priority priority, User creator) {
        return WorkOrderResponse.builder()
                .workOrderId(workOrder.getWorkOrderId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .title(workOrder.getTitle())
                .subCategoryName(subCategory.getName())
                .priorityName(priority.getName())
                .locationDetail(workOrder.getLocationDetail())
                .contactPhone(workOrder.getContactPhone())
                .description(workOrder.getDescription())
                .dueTime(workOrder.getDueTime())
                .status(workOrder.getStatus())
                .createdTime(workOrder.getCreatedTime())
                .creatorName(creator.getName())
                .build();
    }
}
