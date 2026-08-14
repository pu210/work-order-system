package com.eeit219.work_order_system.modules.c.repository;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

public interface WorkOrderRepositoryC extends JpaRepository<WorkOrder, Integer> {
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
