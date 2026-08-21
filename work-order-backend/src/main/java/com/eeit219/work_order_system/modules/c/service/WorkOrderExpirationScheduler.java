package com.eeit219.work_order_system.modules.c.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.service.NotificationService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkOrderExpirationScheduler {

    private final WorkOrderRepository workOrderRepository;
    private final NotificationService notificationService;

    public WorkOrderExpirationScheduler(WorkOrderRepository workOrderRepository,
            NotificationService notificationService) {
        this.workOrderRepository = workOrderRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${scheduler.work-order-overdue.cron:0 */5 * * * *}", zone = "Asia/Taipei")
    @Transactional
    public void markOverdueWorkOrders() {

        LocalDateTime now = LocalDateTime.now();

        List<WorkOrder> overdueWorkOrders = workOrderRepository.findAllByDueTimeBeforeAndIsOverdueFalseAndStatusNotIn(
                now,
                List.of(
                        WorkOrderState.COMPLETED,
                        WorkOrderState.CANCELLED));

        int Count = 0;

        for (WorkOrder workOrder : overdueWorkOrders) {

            workOrder.setIsOverdue(true);
            notificationService.sendNotification(workOrder.getAdmin().getUserId(), null, workOrder.getWorkOrderId(),
                    "有工單預期", "工單：" + workOrder.getWorkOrderNo() + ",已逾期請盡速處理", workOrder.getStatus());
            Count++;

            notificationService.sendNotification(workOrder.getAssignedHandler().getUserId(), null,
                    workOrder.getWorkOrderId(), "有工單預期", "工單：" + workOrder.getWorkOrderNo() + ",已逾期請盡速處理",
                    workOrder.getStatus());
            Count++;

        }

        log.info(
                "本次標記 {} 筆逾期工單，建立 {} 筆通知",
                overdueWorkOrders.size(),
                Count);
    }
}
