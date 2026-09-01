package com.eeit219.work_order_system.modules.d.service;

import com.eeit219.work_order_system.modules.a.entity.Role;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.entity.UserRole;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WorkOrderAuthorizationService {

    // 判斷目前使用者是否為工單建立人。
    public boolean isCreator(WorkOrder workOrder, User currentUser) {
        if (workOrder == null || currentUser == null || workOrder.getCreator() == null) {
            return false;
        }
        return Objects.equals(workOrder.getCreator().getUserId(), currentUser.getUserId());

    }

    // 判斷目前使用者是否為工單目前指派的負責工程師。
    public boolean isAssignedHandler(WorkOrder workOrder, User currentUser) {
        if (workOrder == null || currentUser == null || workOrder.getAssignedHandler() == null) {
            return false;
        }
        return Objects.equals(workOrder.getAssignedHandler().getUserId(), currentUser.getUserId());

    }

    // 判斷目前使用者是否具有管理員角色。
    public boolean isAdmin(User currentUser) {
        if (currentUser == null || currentUser.getUserRoles() == null) {
            return false;
        }
        return currentUser.getUserRoles().stream()
                .filter(Objects::nonNull)
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .map(Role::getRoleCode)
                .filter(Objects::nonNull)
                .anyMatch(Role.ADMIN::equalsIgnoreCase);
    }

    // 查看工單或留言清單
    public void validateViewPermission(WorkOrder workOrder, User currentUser) {

        validateAuthenticated(currentUser);

        boolean canView = isAdmin(currentUser) || isAssignedHandler(workOrder, currentUser) || isCreator(workOrder, currentUser);
        if (!canView) {
            throw new AccessDeniedException("你沒有權限查看此報修單");
        }
    }

    // 新增文字留言
    public void validateCommentPermission(
            WorkOrder workOrder,
            User currentUser) {

        validateAuthenticated(currentUser);

        boolean canComment =
                isAdmin(currentUser)
                        || isCreator(workOrder, currentUser)
                        || isAssignedHandler(workOrder, currentUser);

        if (!canComment) {
            throw new AccessDeniedException(
                    "你沒有權限在此報修單新增留言"
            );
        }
    }

    public void validateAuthenticated(User currentUser){
        if(currentUser == null){
            throw new AccessDeniedException("使用者尚未登入");
        }

    }

}
