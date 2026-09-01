package com.eeit219.work_order_system.modules.e.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.e.dto.DailyReportDto;
import com.eeit219.work_order_system.modules.e.dto.EngineerKpiReportDto;
import com.eeit219.work_order_system.modules.e.dto.MonthlyReportDto;
import com.eeit219.work_order_system.modules.e.service.ReportService;

import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 1. 取得大分類統計報表 (支援日期區間過濾)
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getCategoryReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryReportDto> report = reportService.getCategoryReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢報表成功", report));
    }

    // 2. 取得細項分類統計報表 (支援日期區間過濾)
    @GetMapping("/subcategories")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getSubCategoryReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryReportDto> report = reportService.getSubCategoryReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢報表成功", report));
    }

    // 3. 依狀態統計報表 (支援日期區間過濾)
    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getStatusReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryReportDto> report = reportService.getStatusReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢狀態報表成功", report));
    }

    // 4. 依工單建立者統計報表 (支援日期區間過濾)
    @GetMapping("/creators")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getCreatorReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryReportDto> report = reportService.getCreatorReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢建立者報表成功", report));
    }

    // 5. 依優先級統計報表 (支援日期區間過濾)
    @GetMapping("/priorities")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getPriorityReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryReportDto> report = reportService.getPriorityReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢優先級報表成功", report));
    }

    // 5.1 依設備型號統計報表 (支援日期區間過濾)
    @GetMapping("/equipment-models")
    public ResponseEntity<ApiResponse<List<CategoryReportDto>>> getEquipmentModelReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryReportDto> report = reportService.getEquipmentModelReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢設備型號報表成功", report));
    }

    // 6. 依月份統計報表 (折線圖用)
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyReportDto>>> getMonthlyReport(
            @RequestParam(required = false) Integer year) {
        List<MonthlyReportDto> report = reportService.getMonthlyReport(year);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢月份統計報表成功", report));
    }

    // 7. 依每日統計報表 (折線圖用，支援年份與月份過濾)
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailyReportDto>>> getDailyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        List<DailyReportDto> report = reportService.getDailyReport(year, month);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢每日統計報表成功", report));
    }

    // 8. 取得工程師處理 KPI 報表 (支援日期區間過濾)
    @GetMapping("/engineer-kpi")
    public ResponseEntity<ApiResponse<List<EngineerKpiReportDto>>> getEngineerKpiReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EngineerKpiReportDto> report = reportService.getEngineerKpiReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢工程師 KPI 報表成功", report));
    }

    // 測試用 API：列出目前資料庫內的所有工單
    @GetMapping("/test-work-orders")
    public ResponseEntity<ApiResponse<List<WorkOrder>>> getAllWorkOrders() {
        List<WorkOrder> list = reportService.getAllWorkOrders();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢工單成功", list));
    }
}