package com.eeit219.work_order_system.modules.b.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {

      Optional<WorkOrder> findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(String prefix);

      @Query("SELECT w FROM WorkOrder w " +
                  "JOIN FETCH w.subCategory sc " +
                  "JOIN FETCH sc.repairCategory " +
                  "JOIN FETCH w.priority " +
                  "JOIN FETCH w.creator " +
                  "LEFT JOIN FETCH w.admin " +
                  "LEFT JOIN FETCH w.assignedHandler " +
                  "WHERE w.workOrderId = :workOrderId")
      Optional<WorkOrder> findByIdWithDetails(@Param("workOrderId") Integer workOrderId);

      // 統一給 ADMIN／HANDLER 兩種視角用：restrictToUserId 是 null 時等同「查全部」（ADMIN），
      // 有帶值時限定「建立者或被指派工程師 = 這個人」（HANDLER）。
      // 原本 search()／findRelevantToUser() 兩支查詢除了這個限定條件，其餘 JOIN FETCH 跟篩選子句完全一樣，
      // 合併成一支，避免以後加篩選欄位要同時改兩份幾乎一樣的 JPQL
      @Query(value = "SELECT w FROM WorkOrder w " +
                  "JOIN FETCH w.subCategory sc " +
                  "JOIN FETCH sc.repairCategory " +
                  "JOIN FETCH w.priority " +
                  "JOIN FETCH w.creator " +
                  "LEFT JOIN FETCH w.admin " +
                  "LEFT JOIN FETCH w.assignedHandler " +
                  "WHERE (:restrictToUserId IS NULL OR w.creator.userId = :restrictToUserId OR w.assignedHandler.userId = :restrictToUserId) "
                  +
                  "AND (:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.title LIKE %:keyword% OR w.locationDetail LIKE %:keyword%) "
                  +
                  "AND (:status IS NULL OR w.status = :status) " +
                  "AND (:priorityId IS NULL OR w.priority.prioritiesId = :priorityId) " +
                  "AND (:categoryId IS NULL OR sc.repairCategory.repairCategoriesId = :categoryId) " +
                  "AND (:assignedHandlerId IS NULL OR w.assignedHandler.userId = :assignedHandlerId) " +
                  "AND (:adminUserId IS NULL OR w.admin.userId = :adminUserId)", countQuery = "SELECT COUNT(w) FROM WorkOrder w "
                              +
                              "JOIN w.subCategory sc " +
                              "WHERE (:restrictToUserId IS NULL OR w.creator.userId = :restrictToUserId OR w.assignedHandler.userId = :restrictToUserId) "
                              +
                              "AND (:keyword IS NULL OR w.workOrderNo LIKE %:keyword% OR w.title LIKE %:keyword% OR w.locationDetail LIKE %:keyword%) "
                              +
                              "AND (:status IS NULL OR w.status = :status) " +
                              "AND (:priorityId IS NULL OR w.priority.prioritiesId = :priorityId) " +
                              "AND (:categoryId IS NULL OR sc.repairCategory.repairCategoriesId = :categoryId) "
                              +
                              "AND (:assignedHandlerId IS NULL OR w.assignedHandler.userId = :assignedHandlerId) " +
                              "AND (:adminUserId IS NULL OR w.admin.userId = :adminUserId)")
      Page<WorkOrder> search(@Param("keyword") String keyword,
                  @Param("status") WorkOrderState status,
                  @Param("priorityId") Integer priorityId,
                  @Param("categoryId") Integer categoryId,
                  @Param("assignedHandlerId") Integer assignedHandlerId,
                  @Param("adminUserId") Integer adminUserId,
                  @Param("restrictToUserId") Integer restrictToUserId,
                  Pageable pageable);

      @Query(value = "SELECT w FROM WorkOrder w " +
                  "JOIN FETCH w.subCategory sc " +
                  "JOIN FETCH sc.repairCategory " +
                  "JOIN FETCH w.priority " +
                  "JOIN FETCH w.creator " +
                  "LEFT JOIN FETCH w.admin " +
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

      // 在工單找出逾期且沒被標記的工單
      List<WorkOrder> findAllByDueTimeBeforeAndIsOverdueFalseAndStatusNotIn(
                  LocalDateTime now,
                  Collection<WorkOrderState> excludedStates);

}
