package com.eeit219.work_order_system.modules.d.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;

/**
 * D 模組專用的工單退回紀錄查詢介面。
 *
 * 查詢邏輯留在 D 模組，避免為詳情頁需求修改 C 模組既有的 Repository。
 */
public interface WorkOrderRejectionRecordRepository
        extends JpaRepository<RepairTicketHistory, Integer> {

    /**
     * 依工單與事件查詢歷程，並預先載入操作人員，避免轉換 DTO 時重複查詢。
     */
    @EntityGraph(attributePaths = "editor")
    List<RepairTicketHistory> findByWorkOrderWorkOrderIdAndEventOrderByEditedTimeDescHistoryIdDesc(
            Integer workOrderId,
            WorkOrderEvent event);
}
