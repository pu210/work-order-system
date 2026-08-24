package com.eeit219.work_order_system.modules.e.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.e.dto.DailyReportDto;
import com.eeit219.work_order_system.modules.e.dto.MonthlyReportDto;
import com.eeit219.work_order_system.modules.e.repository.ReportWorkOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportWorkOrderRepository workOrderRepository;

    // 1. 取得大分類統計報表
    public List<CategoryReportDto> getCategoryReport() {
        return workOrderRepository.countWorkOrdersByCategory();
    }

    // 2. 取得細項分類統計報表
    public List<CategoryReportDto> getSubCategoryReport() {
        return workOrderRepository.countWorkOrdersBySubCategory();
    }

    // 3. 依狀態統計報表
    public List<CategoryReportDto> getStatusReport() {
        return workOrderRepository.countWorkOrdersByStatus();
    }

    // 4. 依工單建立者統計報表
    public List<CategoryReportDto> getCreatorReport() {
        return workOrderRepository.countWorkOrdersByCreator();
    }

    // 5. 依優先級統計報表
    public List<CategoryReportDto> getPriorityReport() {
        return workOrderRepository.countWorkOrdersByPriority();
    }

    // 6. 依月份群組統計報表 (折線圖用)
    public List<MonthlyReportDto> getMonthlyReport(Integer year) {
        if (year != null) {
            return workOrderRepository.countWorkOrdersByMonthAndYear(year);
        }
        return workOrderRepository.countWorkOrdersByMonth();
    }

    // 7. 依每日群組統計報表 (折線圖用，支援年份與月份過濾)
    public List<DailyReportDto> getDailyReport(Integer year, Integer month) {
        return workOrderRepository.countWorkOrdersByDaily(year, month);
    }

    // 列出目前資料庫內的所有工單
    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }
}