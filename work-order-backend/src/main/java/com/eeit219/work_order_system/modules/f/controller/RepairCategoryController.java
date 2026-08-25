package com.eeit219.work_order_system.modules.f.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.f.entity.RepairCategory;
import com.eeit219.work_order_system.modules.f.repository.RepairCategoryRepository;

@RestController
@RequestMapping("/api/repair-categories")
public class RepairCategoryController {

    @Autowired
    private RepairCategoryRepository repairCategoryRepository;

    @GetMapping
    public List<RepairCategory> getAllOrSearchCategories(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return repairCategoryRepository.searchByKeyword(keyword);
        } else {
            return repairCategoryRepository.findAll();
        }
    }

    // B 模組用：新增工單頁的大類下拉選單，只回傳啟用中（status = true）的資料。
    // 獨立一支端點，跟上面給系統設定頁用的 getAllOrSearchCategories() 分開，F 模組調整那支時不會影響到。
    @GetMapping("/active")
    public ApiResponse<List<RepairCategory>> getActiveCategories() {
        List<RepairCategory> data = repairCategoryRepository.findByStatusTrue();
        return ApiResponse.success(HttpStatus.OK.value(), "成功", data);
    }

    // B 模組用：需要「全部大類」的地方（例如工單列表篩選下拉選單）改打這支，不用 getAllOrSearchCategories()。
    // getAllOrSearchCategories() 沒帶 keyword 時走 findAll()，對每一筆大類各自補一條 SQL
    // 查defaultPriority。
    @GetMapping("/all-with-priority")
    public ApiResponse<List<RepairCategory>> getAllCategoriesWithPriority() {
        List<RepairCategory> data = repairCategoryRepository.findAllWithDefaultPriority();
        return ApiResponse.success(HttpStatus.OK.value(), "成功", data);
    }

    @PostMapping
    public RepairCategory createCategory(@RequestBody RepairCategory category) {
        category.setCreatedTime(LocalDateTime.now());
        category.setUpdatedTime(LocalDateTime.now());
        if (category.getStatus() == null) {
            category.setStatus(true);
        }
        RepairCategory saved = repairCategoryRepository.save(category);

        // 🌟 關鍵：存檔後重新用 ID 查詢一次，讓 EAGER 關聯順便載進來
        return repairCategoryRepository.findById(saved.getRepairCategoriesId()).orElse(saved);
    }

    @PutMapping("/{repairCategoriesId}")
    public RepairCategory updateCategory(@PathVariable Integer repairCategoriesId,
            @RequestBody RepairCategory categoryDetails) {
        RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

        category.setName(categoryDetails.getName());
        category.setDefaultPriorityId(categoryDetails.getDefaultPriorityId());

        // 🌟 加上這行防呆：有傳 status 才更新，沒傳就保留原本的值
        if (categoryDetails.getStatus() != null) {
            category.setStatus(categoryDetails.getStatus());
        }

        category.setUpdatedTime(LocalDateTime.now());

        RepairCategory saved = repairCategoryRepository.save(category);

        // 🌟 關鍵：更新後也重新用 ID 查詢一次
        return repairCategoryRepository.findById(saved.getRepairCategoriesId()).orElse(saved);
    }

    @PatchMapping("/{repairCategoriesId}/status")
    public RepairCategory updateStatus(@PathVariable Integer repairCategoriesId, @RequestParam Boolean status) {
        RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

        category.setStatus(status);
        category.setUpdatedTime(LocalDateTime.now());

        RepairCategory saved = repairCategoryRepository.save(category);
        return repairCategoryRepository.findById(saved.getRepairCategoriesId()).orElse(saved);
    }
}
