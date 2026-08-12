package com.eeit219.work_order_system.modules.b.service;

import java.time.LocalDateTime;
import java.time.Year;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderCreateRequest;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderListItemResponse;
import com.eeit219.work_order_system.modules.b.dto.WorkOrderResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;
import com.eeit219.work_order_system.modules.f.repository.SubCategoryRepository;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final UserRepository userRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
            SubCategoryRepository subCategoryRepository,
            UserRepository userRepository) {
        this.workOrderRepository = workOrderRepository;
        this.subCategoryRepository = subCategoryRepository;
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
        // due_time 等蹦哥的邏輯在這之前先設成派工流程決定
        workOrder.setDueTime(null);
        workOrder.setCreatedTime(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(workOrder);

        return toResponse(saved, subCategory, priority, creator);
    }

    public WorkOrderResponse getById(Integer workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到工單：" + workOrderId));

        return toResponse(workOrder, workOrder.getSubCategory(), workOrder.getPriority(), workOrder.getCreator());
    }

    public Page<WorkOrderListItemResponse> list(Integer priorityId, Pageable pageable) {
        return workOrderRepository.search(priorityId, pageable).map(this::toListItem);
    }

    private Priority resolvePriority(SubCategory subCategory) {
        if (subCategory.getOverridePriority() != null) {
            return subCategory.getOverridePriority();
        }
        // override_priority 為 null 時，往上抓大類別的預設優先級
        Priority defaultPriority = subCategory.getRepairCategory().getDefaultPriority();
        if (defaultPriority == null) {
            throw new IllegalStateException("子類別與所屬大類別皆未設定優先級：" + subCategory.getSubCategoriesId());
        }
        return defaultPriority;
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
                .categoryName(subCategory.getRepairCategory().getName())
                .subCategoryName(subCategory.getName())
                .priorityName(priority.getName())
                .locationDetail(workOrder.getLocationDetail())
                .contactPhone(workOrder.getContactPhone())
                .description(workOrder.getDescription())
                .dueTime(workOrder.getDueTime())
                .status(workOrder.getStatus().name())
                .createdTime(workOrder.getCreatedTime())
                .creatorName(creator.getName())
                .build();
    }

    private WorkOrderListItemResponse toListItem(WorkOrder workOrder) {
        return WorkOrderListItemResponse.builder()
                .workOrderId(workOrder.getWorkOrderId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .title(workOrder.getTitle())
                .categoryName(workOrder.getSubCategory().getRepairCategory().getName())
                .priorityName(workOrder.getPriority().getName())
                .status(workOrder.getStatus().name())
                .creatorName(workOrder.getCreator().getName())
                .assignedHandlerName(workOrder.getAssignedHandler() != null
                        ? workOrder.getAssignedHandler().getName()
                        : null)
                .createdTime(workOrder.getCreatedTime())
                .build();
    }
}
