package com.eeit219.work_order_system.modules.e.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.e.dto.DailyReportDto;
import com.eeit219.work_order_system.modules.e.dto.MonthlyReportDto;
import org.springframework.data.repository.query.Param;

public interface ReportWorkOrderRepository extends JpaRepository<WorkOrder, Integer> {

    // 1. 依大分類群組統計
    @Query("SELECT sc.repairCategory.name AS categoryName, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w JOIN w.subCategory sc " +
            "GROUP BY sc.repairCategory.name")
    List<CategoryReportDto> countWorkOrdersByCategory();

    // 2. 依細項分類群組統計
    @Query("SELECT sc.name AS subCategoryName, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w JOIN w.subCategory sc " +
            "GROUP BY sc.name")
    List<CategoryReportDto> countWorkOrdersBySubCategory();

    // 3. 依工單狀態群組統計
    @Query("SELECT CAST(w.status AS string) AS statusName, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w " +
            "GROUP BY w.status")
    List<CategoryReportDto> countWorkOrdersByStatus();

    // 4. 依建立者群組統計
    @Query("SELECT c.name AS creatorName, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w JOIN w.creator c " +
            "GROUP BY c.name")
    List<CategoryReportDto> countWorkOrdersByCreator();

    // 5. 依優先級群組統計
    @Query("SELECT p.name AS priorityName, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w JOIN w.priority p " +
            "GROUP BY p.name")
    List<CategoryReportDto> countWorkOrdersByPriority();

    // 6. 依月份群組統計 (全年份)
    @Query("SELECT YEAR(w.createdTime) AS year, MONTH(w.createdTime) AS month, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w " +
            "WHERE w.createdTime IS NOT NULL " +
            "GROUP BY YEAR(w.createdTime), MONTH(w.createdTime) " +
            "ORDER BY YEAR(w.createdTime) ASC, MONTH(w.createdTime) ASC")
    List<MonthlyReportDto> countWorkOrdersByMonth();

    // 7. 依指定年份之月份群組統計
    @Query("SELECT YEAR(w.createdTime) AS year, MONTH(w.createdTime) AS month, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w " +
            "WHERE w.createdTime IS NOT NULL AND YEAR(w.createdTime) = :year " +
            "GROUP BY YEAR(w.createdTime), MONTH(w.createdTime) " +
            "ORDER BY MONTH(w.createdTime) ASC")
    List<MonthlyReportDto> countWorkOrdersByMonthAndYear(@Param("year") Integer year);

    // 8. 依每日群組統計 (支援指定年份與/或指定月份)
    @Query("SELECT YEAR(w.createdTime) AS year, MONTH(w.createdTime) AS month, DAY(w.createdTime) AS day, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w " +
            "WHERE w.createdTime IS NOT NULL " +
            "AND (:year IS NULL OR YEAR(w.createdTime) = :year) " +
            "AND (:month IS NULL OR MONTH(w.createdTime) = :month) " +
            "GROUP BY YEAR(w.createdTime), MONTH(w.createdTime), DAY(w.createdTime) " +
            "ORDER BY YEAR(w.createdTime) ASC, MONTH(w.createdTime) ASC, DAY(w.createdTime) ASC")
    List<DailyReportDto> countWorkOrdersByDaily(@Param("year") Integer year, @Param("month") Integer month);
}