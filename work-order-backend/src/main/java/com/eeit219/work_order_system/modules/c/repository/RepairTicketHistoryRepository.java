package com.eeit219.work_order_system.modules.c.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

public interface RepairTicketHistoryRepository extends JpaRepository<RepairTicketHistory, Integer> {

    List<RepairTicketHistory> findByWorkOrderWorkOrderIdOrderByEditedTimeAscHistoryIdAsc(Integer workOrderId);

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

    @EntityGraph(attributePaths = {
            "workOrder.subCategory.repairCategory",
            "workOrder.priority",
            "workOrder.creator",
            "workOrder.assignedHandler"
    })
    @Query(
            value = "SELECT h FROM RepairTicketHistory h " +
                    "JOIN h.workOrder w " +
                    "WHERE w.repairTarget.targetNo = :targetNo " +
                    "AND w.status = :completedStatus " +
                    "AND h.status = :completedStatus " +
                    "AND (:completedAfter IS NULL OR h.editedTime >= :completedAfter) " +
                    "ORDER BY h.editedTime DESC",
            countQuery = "SELECT COUNT(h) FROM RepairTicketHistory h " +
                    "JOIN h.workOrder w " +
                    "WHERE w.repairTarget.targetNo = :targetNo " +
                    "AND w.status = :completedStatus " +
                    "AND h.status = :completedStatus " +
                    "AND (:completedAfter IS NULL OR h.editedTime >= :completedAfter)"
    )
    Page<RepairTicketHistory> findCompletedEquipmentHistory(
            @Param("targetNo") String targetNo,
            @Param("completedStatus") WorkOrderState completedStatus,
            @Param("completedAfter") LocalDateTime completedAfter,
            Pageable pageable);
}
