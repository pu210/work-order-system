package com.eeit219.work_order_system.modules.d.service;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderDetailResponse;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkOrderDetailService {

    private final WorkOrderDetailRepository workOrderDetailRepository;

    private final WorkOrderAuthorizationService workOrderAuthorizationService;

    // 報修單詳情
    public WorkOrderDetailResponse getWorkOrderDetail(
            Integer workOrderId,
            User currentUser) {

        workOrderAuthorizationService.validateAuthenticated(currentUser);

        WorkOrder workOrder = workOrderDetailRepository
                .findDetailById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到報修單，ID：" + workOrderId));

        workOrderAuthorizationService.validateViewPermission(workOrder, currentUser);

        return convertToDetailResponse(workOrder);
    }

    // 將 WorkOrder Entity 轉換成詳情 DTO。
    private WorkOrderDetailResponse convertToDetailResponse(
            WorkOrder workOrder) {

        User creator = workOrder.getCreator();
        User assignedHandler = workOrder.getAssignedHandler();

        Integer creatorUserId = null;
        String creatorName = null;

        if (creator != null) {
            creatorUserId = creator.getUserId();
            creatorName = creator.getName();
        }

        Integer assignedHandlerId = null;
        String assignedHandlerName = null;

        if (assignedHandler != null) {
            assignedHandlerId = assignedHandler.getUserId();

            assignedHandlerName = assignedHandler.getName();
        }

        String subCategoryName = null;
        String categoryName = null;

        if (workOrder.getSubCategory() != null) {
            subCategoryName = workOrder.getSubCategory()
                    .getName();

            if (workOrder.getSubCategory().getRepairCategory() != null) {
                categoryName = workOrder.getSubCategory()
                        .getRepairCategory()
                        .getName();
            }
        }

        String priorityName = null;

        if (workOrder.getPriority() != null) {
            priorityName = workOrder.getPriority()
                    .getName();
        }

        return WorkOrderDetailResponse.builder()
                .workOrderId(workOrder.getWorkOrderId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .title(workOrder.getTitle())
                .categoryName(categoryName)
                .subCategoryName(subCategoryName)
                .priorityName(priorityName)
                .locationDetail(workOrder.getLocationDetail())
                .contactPhone(workOrder.getContactPhone())
                .description(workOrder.getDescription())
                .dueTime(workOrder.getDueTime())
                .status(String.valueOf(workOrder.getStatus()))
                .createdTime(workOrder.getCreatedTime())
                .creatorUserId(creatorUserId)
                .creatorName(creatorName)
                .assignedHandlerId(assignedHandlerId)
                .assignedHandlerName(assignedHandlerName)
                .isOverDue(workOrder.getIsOverdue())
                .version(workOrder.getVersion())
                .build();
    }

}
