package com.eeit219.work_order_system.modules.e.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.e.entity.WorkOrder;
import com.eeit219.work_order_system.modules.e.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 取得大分類統計報表
    @GetMapping("/categories")
    public List<CategoryReportDto> getCategoryReport() {
        return reportService.getCategoryReport();
    }

    // 測試用 API：列出目前資料庫內的所有工單
    @GetMapping("/test-work-orders")
    public List<WorkOrder> getAllWorkOrders() {
        return reportService.getAllWorkOrders();
    }

    // 測試用 API：自動在資料庫建立一筆測試工單
    @GetMapping("/test-create-sample")
    @PostMapping("/test-create-sample")
    public WorkOrder createSampleWorkOrder() {
        return reportService.createSampleWorkOrder();
    }
}