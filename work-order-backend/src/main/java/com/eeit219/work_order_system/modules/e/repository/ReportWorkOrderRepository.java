package com.eeit219.work_order_system.modules.e.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;

public interface ReportWorkOrderRepository extends JpaRepository<WorkOrder, Integer> {

    // 使用 HQL LEFT JOIN 進行群組統計 (防止 sub_category_id 為 NULL 或未與 sub_categories
    // 連結時被過濾掉)
    // @Query("SELECT COALESCE(s.name, '未指定細項') AS categoryName,
    // COUNT(w.workOrderId) AS count " +
    // "FROM WorkOrder w LEFT JOIN w.subCategory s " +
    // "GROUP BY s.subCategoryId, s.name")
    // List<CategoryReportDto> countWorkOrdersByCategory();

    @Query("SELECT sc.repairCategory.name AS categoryName, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w JOIN w.subCategory sc " +
            "GROUP BY sc.repairCategory.name")
    List<CategoryReportDto> countWorkOrdersByCategory();

    @Query("SELECT sc.name AS subCategoryName, COUNT(w.workOrderId) AS count " +
            "FROM WorkOrder w JOIN w.subCategory sc " +
            "GROUP BY sc.name")
    List<CategoryReportDto> countWorkOrdersBySubCategory();
}