package com.eeit219.work_order_system.modules.c.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;

public interface RepairTicketHistoryRepository extends JpaRepository<RepairTicketHistory, Integer> {

    Optional<RepairTicketHistory> findTopByWorkOrderWorkOrderIdAndEventOrderByHistoryIdDesc(
            Integer workOrderId, WorkOrderEvent event);

    @Query("SELECT h FROM RepairTicketHistory h " +
           "JOIN FETCH h.workOrder w " +
           "JOIN FETCH h.editor e " +
           "WHERE (:startDate IS NULL OR h.editedTime >= :startDate) " +
           "AND (:endDate IS NULL OR h.editedTime <= :endDate) " +
           "ORDER BY h.workOrder.workOrderId ASC, h.editedTime ASC")
    List<RepairTicketHistory> findHistoryForKpiReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
