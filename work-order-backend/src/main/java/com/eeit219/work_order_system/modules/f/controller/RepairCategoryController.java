package com.eeit219.work_order_system.modules.f.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse; // 引入統一回應類別
import com.eeit219.work_order_system.modules.f.dto.RepairCategoryRequestDto;
import com.eeit219.work_order_system.modules.f.dto.RepairCategoryResponseDto;
import com.eeit219.work_order_system.modules.f.entity.RepairCategory;
import com.eeit219.work_order_system.modules.f.repository.RepairCategoryRepository;
import com.eeit219.work_order_system.modules.f.service.RepairCategoryService;

@RestController
@RequestMapping("/api/repair-categories")
public class RepairCategoryController {

    @Autowired
    private RepairCategoryRepository repairCategoryRepository;

    @Autowired
    private RepairCategoryService repairCategoryService;

    @GetMapping
    public ApiResponse<List<RepairCategory>> getAllOrSearchCategories(@RequestParam(required = false) String keyword) {
        List<RepairCategory> result;
        if (keyword != null && !keyword.trim().isEmpty()) {
            result = repairCategoryRepository.searchByKeyword(keyword);
        } else {
            result = repairCategoryRepository.findAll();
        }
        return ApiResponse.success(200, "查詢成功", result);
    }

    // 🌟 專門給細項下拉選單用的 API（透過 Service 回傳 DTO）
    @GetMapping("/active")
    public ApiResponse<List<RepairCategoryResponseDto>> getActiveCategories() {
        List<RepairCategoryResponseDto> result = repairCategoryService.getActiveCategories();
        return ApiResponse.success(200, "查詢啟用中的報修大類成功", result);
    }

    @PostMapping
    public ApiResponse<RepairCategory> createCategory(@RequestBody RepairCategoryRequestDto request) {
        RepairCategory saved = repairCategoryService.createCategory(request);
        return ApiResponse.success(200, "新增報修大類成功", saved);
    }

    @PutMapping("/{repairCategoriesId}")
    public ApiResponse<RepairCategory> updateCategory(@PathVariable Integer repairCategoriesId,
            @RequestBody RepairCategoryRequestDto request) {
        RepairCategory updated = repairCategoryService.updateCategory(repairCategoriesId, request);
        return ApiResponse.success(200, "更新報修大類成功", updated);
    }

    @PatchMapping("/{repairCategoriesId}/status")
    public ApiResponse<RepairCategory> updateStatus(@PathVariable Integer repairCategoriesId,
            @RequestParam Boolean status) {
        RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

        category.setStatus(status);
        category.setUpdatedTime(java.time.LocalDateTime.now());

        RepairCategory saved = repairCategoryRepository.save(category);
        RepairCategory result = repairCategoryRepository.findById(saved.getRepairCategoriesId()).orElse(saved);

        return ApiResponse.success(200, "更新報修大類狀態成功", result);
    }
}