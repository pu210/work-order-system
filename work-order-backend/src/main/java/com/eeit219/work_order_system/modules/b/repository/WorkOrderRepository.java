package com.eeit219.work_order_system.modules.b.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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

    @Query("SELECT w FROM WorkOrder w " +
            "JOIN FETCH w.subCategory sc " +
            "JOIN FETCH sc.repairCategory " +
            "JOIN FETCH w.priority " +
            "JOIN FETCH w.creator " +
            "LEFT JOIN FETCH w.assignedHandler " +
            "WHERE w.workOrderId = :workOrderId")
    Optional<WorkOrder> findByIdWithDetails(@Param("workOrderId") Integer workOrderId);

    @Query(value = "SELECT w FROM WorkOrder w " +
            "JOIN FETCH w.subCategory sc " +
            "JOIN FETCH sc.repairCategory " +
            "JOIN FETCH w.priority " +
            "JOIN FETCH w.creator " +
            "LEFT JOIN FETCH w.assignedHandler " +
            "WHERE (:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.title LIKE %:keyword% OR w.locationDetail LIKE %:keyword%) "
            +
            "AND (:status IS NULL OR w.status = :status) " +
            "AND (:priorityId IS NULL OR w.priority.prioritiesId = :priorityId) " +
            "AND (:categoryId IS NULL OR sc.repairCategory.repairCategoriesId = :categoryId) " +
            "AND (:assignedHandlerId IS NULL OR w.assignedHandler.userId = :assignedHandlerId)", countQuery = "SELECT COUNT(w) FROM WorkOrder w "
                    +
                    "JOIN w.subCategory sc " +
                    "WHERE (:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.title LIKE %:keyword% OR w.locationDetail LIKE %:keyword%) "
                    +
                    "AND (:status IS NULL OR w.status = :status) " +
                    "AND (:priorityId IS NULL OR w.priority.prioritiesId = :priorityId) " +
                    "AND (:categoryId IS NULL OR sc.repairCategory.repairCategoriesId = :categoryId) "
                    +
                    "AND (:assignedHandlerId IS NULL OR w.assignedHandler.userId = :assignedHandlerId)")
    Page<WorkOrder> search(@Param("keyword") String keyword,
            @Param("status") WorkOrderState status,
            @Param("priorityId") Integer priorityId,
            @Param("categoryId") Integer categoryId,
            @Param("assignedHandlerId") Integer assignedHandlerId,
            Pageable pageable);

    @Query(value = "SELECT w FROM WorkOrder w " +
            "JOIN FETCH w.subCategory sc " +
            "JOIN FETCH sc.repairCategory " +
            "JOIN FETCH w.priority " +
            "JOIN FETCH w.creator " +
            "LEFT JOIN FETCH w.assignedHandler " +
            "WHERE w.creator.userId = :creatorId " +
            "AND (:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.title LIKE %:keyword% OR w.locationDetail LIKE %:keyword%) "
            +
            "AND (:status IS NULL OR w.status = :status)", countQuery = "SELECT COUNT(w) FROM WorkOrder w "
                    +
                    "WHERE w.creator.userId = :creatorId " +
                    "AND (:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.title LIKE %:keyword% OR w.locationDetail LIKE %:keyword%) "
                    +
                    "AND (:status IS NULL OR w.status = :status)")
    Page<WorkOrder> findMySubmissions(@Param("keyword") String keyword,
            @Param("status") WorkOrderState status,
            @Param("creatorId") Integer creatorId,
            Pageable pageable);

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
