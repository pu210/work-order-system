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

    // 1. 取得大分類統計報表
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getCategoryReport() {
        List<CategoryReportDto> report = reportService.getCategoryReport();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢報表成功", report));
    }

    // 2. 取得細項分類統計報表
    @GetMapping("/subcategories")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getSubCategoryReport() {
        List<CategoryReportDto> report = reportService.getSubCategoryReport();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢報表成功", report));
    }

    // 3. 依狀態統計報表
    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getStatusReport() {
        List<CategoryReportDto> report = reportService.getStatusReport();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢狀態報表成功", report));
    }

    // 4. 依工單建立者統計報表
    @GetMapping("/creators")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getCreatorReport() {
        List<CategoryReportDto> report = reportService.getCreatorReport();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢建立者報表成功", report));
    }

    // 5. 依優先級統計報表
    @GetMapping("/priorities")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getPriorityReport() {
        List<CategoryReportDto> report = reportService.getPriorityReport();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢優先級報表成功", report));
    }

    // 測試用 API：列出目前資料庫內的所有工單
    @GetMapping("/test-work-orders")
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getAllWorkOrders() {
        List<WorkOrder> list = reportService.getAllWorkOrders();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢工單成功", list));
    }
}