package com.eeit219.work_order_system.modules.b.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {

        boolean existsByWorkOrderNo(String workOrderNo);

        Optional<WorkOrder> findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(String prefix);

        @Query(value = "SELECT w FROM WorkOrder w " +
                        "JOIN FETCH w.subCategory sc " +
                        "JOIN FETCH sc.category " +
                        "JOIN FETCH w.priority " +
                        "JOIN FETCH w.creator " +
                        "LEFT JOIN FETCH w.assignedHandler " +
                        "WHERE (:priorityId IS NULL OR w.priority.prioritiesId = :priorityId)", countQuery = "SELECT COUNT(w) FROM WorkOrder w "
                                        +
                                        "WHERE (:priorityId IS NULL OR w.priority.prioritiesId = :priorityId)")
        Page<WorkOrder> search(@Param("priorityId") Integer priorityId, Pageable pageable);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        UPDATE WorkOrder w
                           SET w.isOverdue = true,
                               w.version = w.version + 1
                         WHERE w.dueTime IS NOT NULL
                           AND w.dueTime < :now
                           AND w.isOverdue = false
                           AND w.status NOT IN :excludedStates
                        """)
        int markOverdue(
                        @Param("now") LocalDateTime now,
                        @Param("excludedStates") Collection<WorkOrderState> excludedStates);
}
