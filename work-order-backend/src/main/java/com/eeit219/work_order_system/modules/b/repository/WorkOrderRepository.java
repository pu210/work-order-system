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

      // HANDLER 視角列表：只回「自己建立」或「被指派」的工單。用在 WorkOrderService.list() 依角色縮限範圍，
      // 讓 GET /api/work-orders 對非管理員也是後端真正把關，不是只靠前端過濾
      @Query(value = "SELECT w FROM WorkOrder w " +
                  "JOIN FETCH w.subCategory sc " +
                  "JOIN FETCH sc.repairCategory " +
                  "JOIN FETCH w.priority " +
                  "JOIN FETCH w.creator " +
                  "LEFT JOIN FETCH w.assignedHandler " +
                  "WHERE (w.creator.userId = :userId OR w.assignedHandler.userId = :userId) " +
                  "AND (:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.title LIKE %:keyword% OR w.locationDetail LIKE %:keyword%) "
                  +
                  "AND (:status IS NULL OR w.status = :status) " +
                  "AND (:priorityId IS NULL OR w.priority.prioritiesId = :priorityId) " +
                  "AND (:categoryId IS NULL OR sc.repairCategory.repairCategoriesId = :categoryId)", countQuery = "SELECT COUNT(w) FROM WorkOrder w "
                              +
                              "JOIN w.subCategory sc " +
                              "WHERE (w.creator.userId = :userId OR w.assignedHandler.userId = :userId) " +
                              "AND (:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.title LIKE %:keyword% OR w.locationDetail LIKE %:keyword%) "
                              +
                              "AND (:status IS NULL OR w.status = :status) " +
                              "AND (:priorityId IS NULL OR w.priority.prioritiesId = :priorityId) " +
                              "AND (:categoryId IS NULL OR sc.repairCategory.repairCategoriesId = :categoryId)")
      Page<WorkOrder> findRelevantToUser(@Param("keyword") String keyword,
                  @Param("status") WorkOrderState status,
                  @Param("priorityId") Integer priorityId,
                  @Param("categoryId") Integer categoryId,
                  @Param("userId") Integer userId,
                  Pageable pageable);

      //在工單找出逾期且沒被標記的工單
      List<WorkOrder> findAllByDueTimeBeforeAndIsOverdueFalseAndStatusNotIn(
                  LocalDateTime now,
                  Collection<WorkOrderState> excludedStates);

}
