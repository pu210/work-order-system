package com.eeit219.work_order_system.modules.c.service;

import java.time.LocalDateTime;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.common.exception.InvalidWorkOrderStateException;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.RejectWorkOrderRequest;
import com.eeit219.work_order_system.modules.c.dto.ReviewAcceptRequest;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.service.NotificationService;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;

import jakarta.transaction.Transactional;

@Service
public class ReviewService {
        private final WorkOrderRepository workOrderRepository;
        private final PriorityRepository priorityRepository;
        private final UserRepository userRepository;
        private final WorkOrderStateMachineService workOrderStateMachineService;
        private final NotificationService notificationService;
        private final EditSessionService editSessionService;

        public ReviewService(WorkOrderRepository workOrderRepository,
                        PriorityRepository priorityRepository,
                        UserRepository userRepository,
                        WorkOrderStateMachineService workOrderStateMachineService,
                        NotificationService notificationService,
                        EditSessionService editSessionService) {
                this.workOrderRepository = workOrderRepository;
                this.priorityRepository = priorityRepository;
                this.userRepository = userRepository;
                this.workOrderStateMachineService = workOrderStateMachineService;
                this.notificationService = notificationService;
                this.editSessionService = editSessionService;
        }

        @Transactional
        public void reviewAccept(ReviewAcceptRequest request, Integer workOrderId, Integer userId,
                        String sessionToken) {
                WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));
                if (workOrder.getStatus() != WorkOrderState.PENDING_REVIEW) {
                        throw new InvalidWorkOrderStateException("目前不是待審查狀態");
                }
                // 判斷是否有指定的負責管理員
                boolean firstReview = workOrder.getAdmin() == null;

                if (firstReview) {
                        // 第一次審查，必須驗證 Edit Session。
                        editSessionService.validate(
                                        workOrderId,
                                        userId,
                                        sessionToken);
                } else {
                        // 不是第一次審查，只允許原本的管理員操作。
                        if (!workOrder.getAdmin()
                                        .getUserId()
                                        .equals(userId)) {

                                throw new AccessDeniedException(
                                                "只有原審核管理員可以操作此工單");
                        }
                }
                // 第一次審查才設定負責管理員。
                if (firstReview) {
                        User admin = userRepository.findById(userId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "找不到該管理員"));

                        workOrder.setAdmin(admin);
                }
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime oldDueTime = workOrder.getDueTime();
                LocalDateTime newDueTime = request.dueTime();
                boolean dueTimeChanged = oldDueTime == null
                                || !oldDueTime.isEqual(newDueTime);

                // 第一次設定期限，或管理員有修改期限時，
                // 新期限必須晚於現在。
                if (dueTimeChanged && !newDueTime.isAfter(now)) {
                        throw new IllegalArgumentException(
                                        "修改後的預計完成時間必須晚於現在");
                }

                Priority priority = priorityRepository.findById(request.priorityId())
                                .orElseThrow(() -> new ResourceNotFoundException("找不到優先級"));
                User handler = userRepository.findActiveHandlerById(request.assignedHandlerId())
                                .orElseThrow(() -> new IllegalArgumentException("指定的使用者不存在、已停用，或不是工程師"));
                workOrder.setPriority(priority);
                workOrder.setAssignedHandler(handler);
                workOrder.setDueTime(newDueTime);

                // 重新計算當下是否逾期
                boolean overdue = newDueTime.isBefore(now);
                workOrder.setIsOverdue(overdue);
                // 執行狀態轉換
                workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                                WorkOrderEvent.ACCEPT);
                // 寫入WorkOrder table
                workOrderRepository.save(workOrder);
                // 第一次審核才需要釋放編輯鎖
                if (firstReview) {
                        editSessionService.release(
                                        workOrderId,
                                        userId,
                                        sessionToken);
                }
                // 傳送訊息給指派工程師
                notificationService.sendNotification(
                                workOrder.getAssignedHandler().getUserId(),
                                userId,
                                workOrderId,
                                "有新任務！",
                                "工單：" + workOrder.getWorkOrderNo() + ",已指派給你，請確認",
                                workOrder.getStatus());
                // 傳送訊息給使用者
                notificationService.sendNotification(
                                workOrder.getCreator().getUserId(),
                                userId,
                                workOrderId,
                                "您所建立的工單由管理員審查通過！",
                                "工單：" + workOrder.getWorkOrderNo() + ",管理員審查通過，並已指派負責工程師："
                                                + workOrder.getAssignedHandler().getName(),
                                workOrder.getStatus());

        }

        @Transactional
        public void reviewReject(RejectWorkOrderRequest request, Integer workOrderId, Integer userId,
                        String sessionToken) {
                WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                                .orElseThrow(() -> new ResourceNotFoundException("找不到工單"));

                if (workOrder.getStatus() != WorkOrderState.PENDING_REVIEW) {
                        throw new InvalidWorkOrderStateException("目前不是待審查狀態");
                }
                // 工單如果是第一次審核就填寫負責管理員,如果不是就驗證是否是負責工單的管理員
                // 判斷是否有指定的負責管理員
                boolean firstReview = workOrder.getAdmin() == null;

                if (firstReview) {
                        // 第一次審查，必須驗證 Edit Session。
                        editSessionService.validate(
                                        workOrderId,
                                        userId,
                                        sessionToken);
                } else {
                        // 不是第一次審查，只允許原本的管理員操作。
                        if (!workOrder.getAdmin()
                                        .getUserId()
                                        .equals(userId)) {

                                throw new AccessDeniedException(
                                                "只有原審核管理員可以操作此工單");
                        }
                }

                // 第一次審查才設定負責管理員。
                if (firstReview) {
                        User admin = userRepository.findById(userId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "找不到該管理員"));

                        workOrder.setAdmin(admin);
                }
                // 執行狀態轉換
                workOrderStateMachineService.changeState(workOrder, userId, request.feedback(),
                                WorkOrderEvent.REJECT);
                // 寫入WorkOrder table
                workOrderRepository.save(workOrder);

                // 第一次審核才需要釋放編輯鎖
                if (firstReview) {
                        editSessionService.release(
                                        workOrderId,
                                        userId,
                                        sessionToken);
                }
                // 傳送訊息給使用者
                notificationService.sendNotification(
                                workOrder.getCreator().getUserId(),
                                userId,
                                workOrderId,
                                "審查拒絕！",
                                "工單：" + workOrder.getWorkOrderNo() + ",已被管理員拒絕,原因：" + request.feedback(),
                                workOrder.getStatus());

        }

}
