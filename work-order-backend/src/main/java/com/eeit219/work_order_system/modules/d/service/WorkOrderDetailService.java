package com.eeit219.work_order_system.modules.d.service;

import com.eeit219.work_order_system.modules.a.entity.Role;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.entity.UserRole;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.d.dto.WorkOrderDetailResponse;
import com.eeit219.work_order_system.modules.d.repository.WorkOrderDetailRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkOrderDetailService {

    private final WorkOrderDetailRepository workOrderDetailRepository;

    // 報修單詳情
    public WorkOrderDetailResponse getWorkOrderDetail(Integer workOrderId, User currentUser) throws AccessDeniedException {
        if(currentUser == null){
            throw new AccessDeniedException("使用者尚未登入");
        }
        Optional<WorkOrder> optionalWorkOrder = workOrderDetailRepository.findDetailById(workOrderId);

        if(optionalWorkOrder.isEmpty()){
            throw new EntityNotFoundException("找不到報修單，ID："+workOrderId);
        }

        WorkOrder workOrder = optionalWorkOrder.get();

        validateViewPermission(workOrder, currentUser);

        return convertToDetailResponse(workOrder);
    }

    // 驗證查看權限
    public void validateViewPermission(WorkOrder workOrder, User currentUser) throws AccessDeniedException {
        boolean isAdmin = hasRole(currentUser, Role.ADMIN);
        boolean isReporter = false;

        if (workOrder.getCreator() != null) {
            Integer reporterId =
                    workOrder.getCreator().getUserId();

            Integer currentUserId =
                    currentUser.getUserId();

            isReporter = Objects.equals(
                    reporterId,
                    currentUserId
            );
        }

        boolean isAssignee = false;

        if (workOrder.getAssignedHandler() != null) {
            Integer assigneeId =
                    workOrder.getAssignedHandler().getUserId();

            Integer currentUserId =
                    currentUser.getUserId();

            isAssignee = Objects.equals(
                    assigneeId,
                    currentUserId
            );
        }

        if (!isAdmin && !isReporter && !isAssignee) {
            throw new AccessDeniedException(
                    "你沒有權限查看此報修單");
        }
    }

    //判斷使用者是否具有指定角色
    private boolean hasRole(
            User user,
            String expectedRoleCode) {
        if(user == null){
            return  false;
        }

        if(user.getUserRoles() == null) {
            return  false;
        }
        for (UserRole userRole : user.getUserRoles()) {

            if (userRole == null) {
                continue;
            }

            Role role = userRole.getRole();

            if (role == null) {
                continue;
            }

            String actualRoleCode = role.getRoleCode();

            if (actualRoleCode == null) {
                continue;
            }

            if (expectedRoleCode.equalsIgnoreCase(actualRoleCode)) {
                return true;
            }
        }

        return false;
    }

    //將 WorkOrder Entity 轉換成詳情 DTO。
    private WorkOrderDetailResponse convertToDetailResponse(
            WorkOrder workOrder) {

        User creator = workOrder.getCreator();
        User assignedHandler =
                workOrder.getAssignedHandler();

        Integer creatorUserId = null;
        String creatorName = null;

        if (creator != null) {
            creatorUserId = creator.getUserId();
            creatorName = creator.getName();
        }

        Integer assignedHandlerId = null;
        String assignedHandlerName = null;

        if (assignedHandler != null) {
            assignedHandlerId =
                    assignedHandler.getUserId();

            assignedHandlerName =
                    assignedHandler.getName();
        }

        String subCategoryName = null;
        String categoryName = null;

        if (workOrder.getSubCategory() != null) {
            subCategoryName =
                    workOrder.getSubCategory()
                            .getName();

            if (workOrder.getSubCategory().getRepairCategory() != null) {
                categoryName = workOrder.getSubCategory()
                        .getRepairCategory()
                        .getName();
            }
        }

        String priorityName = null;

        if (workOrder.getPriority() != null) {
            priorityName =
                    workOrder.getPriority()
                            .getName();
        }

        return WorkOrderDetailResponse.builder()
                .workOrderId(workOrder.getWorkOrderId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .title(workOrder.getTitle())
                .categoryName(categoryName)
                .subCategoryName(subCategoryName)
                .priorityName(priorityName)
                .locationDetail(
                        workOrder.getLocationDetail()
                )
                .contactPhone(
                        workOrder.getContactPhone()
                )
                .description(
                        workOrder.getDescription()
                )
                .dueTime(workOrder.getDueTime())
                .status(String.valueOf(workOrder.getStatus()))
                .createdTime(
                        workOrder.getCreatedTime()
                )
                .creatorUserId(creatorUserId)
                .creatorName(creatorName)
                .assignedHandlerId(
                        assignedHandlerId
                )
                .assignedHandlerName(
                        assignedHandlerName
                )
                .isOverDue(
                        workOrder.getIsOverdue()
                )
                .build();
    }

}
