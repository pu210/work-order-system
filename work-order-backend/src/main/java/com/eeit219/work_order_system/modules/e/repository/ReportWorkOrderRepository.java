package com.eeit219.work_order_system.modules.e.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;

public interface ReportWorkOrderRepository extends JpaRepository<WorkOrder, Long> {

	// 使用 Hibernate / Spring Data 的 Native Query (原生 SQL 語法) 進行 GROUP BY 大分類統計
	@Query("SELECT w.category.name AS categoryName, COUNT(w.id) AS count " +
			"FROM WorkOrder w " +
			"GROUP BY w.category.name")
	List<CategoryReportDto> countWorkOrdersByCategory(); // 回傳 介面不用寫RETURN
}