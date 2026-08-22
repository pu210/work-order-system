package com.eeit219.work_order_system.modules.e.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
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
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getCategoryReport() {
        List<CategoryReportDto> report = reportService.getCategoryReport();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢報表成功", report));
    }

    // 取得細項分類統計報表
    @GetMapping("/subcategories")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getSubCategoryReport() {
        List<CategoryReportDto> report = reportService.getSubCategoryReport();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢報表成功", report));
    }

    // 測試用 API：列出目前資料庫內的所有工單
    @GetMapping("/test-work-orders")
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getAllWorkOrders() {
        List<WorkOrder> list = reportService.getAllWorkOrders();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢工單成功", list));
    }
}