package com.eeit219.work_order_system.modules.d.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;

/**
 * 工單詳情頁的流程回饋查詢介面。
 */
public interface WorkOrderFeedbackRecordRepository
        extends JpaRepository<RepairTicketHistory, Integer> {

    @EntityGraph(attributePaths = "editor")
    List<RepairTicketHistory> findByWorkOrderWorkOrderIdAndEventOrderByEditedTimeDescHistoryIdDesc(
            Integer workOrderId,
            WorkOrderEvent event);
}
