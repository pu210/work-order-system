package com.eeit219.work_order_system.modules.e.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;

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
}