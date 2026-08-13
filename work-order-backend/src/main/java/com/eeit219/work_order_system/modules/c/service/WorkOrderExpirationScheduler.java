package com.eeit219.work_order_system.modules.c.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderExpirationScheduler {

    private final WorkOrderRepository workOrderRepository;

    @Scheduled(cron = "${scheduler.work-order-overdue.cron:0 */5 * * * *}", zone = "Asia/Taipei")
    @Transactional
    public void markOverdueWorkOrders() {
        int count = workOrderRepository.markOverdue(
                LocalDateTime.now(),
                List.of(
                        WorkOrderState.COMPLETED,
                        WorkOrderState.CANCELLED));

        log.info("本次標記 {} 筆逾期工單", count);
    }
}
