package com.eeit219.work_order_system.modules.F.controller;

import java.time.LocalDateTime;
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

import com.eeit219.work_order_system.modules.F.entity.RepairCategory;
import com.eeit219.work_order_system.modules.F.repository.RepairCategoryRepository;

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

    @PostMapping
    public RepairCategory createCategory(@RequestBody RepairCategory category) {
        category.setCreatedTime(LocalDateTime.now());
        category.setUpdatedTime(LocalDateTime.now());
        if (category.getStatus() == null) {
            category.setStatus(true);
        }
        return repairCategoryRepository.save(category);
    }

    @PutMapping("/{repairCategoriesId}")
    public RepairCategory updateCategory(@PathVariable Integer repairCategoriesId,
            @RequestBody RepairCategory categoryDetails) {
        RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

        category.setName(categoryDetails.getName());
        category.setName(categoryDetails.getName());
        category.setDefaultPriorityId(categoryDetails.getDefaultPriorityId());
        category.setStatus(categoryDetails.getStatus());
        category.setUpdatedTime(LocalDateTime.now());

        return repairCategoryRepository.save(category);
    }

    @PatchMapping("/{repairCategoriesId}/status")
    public RepairCategory updateStatus(@PathVariable Integer repairCategoriesId, @RequestParam Boolean status) {
        RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

        category.setStatus(status);
        category.setUpdatedTime(LocalDateTime.now());

        return repairCategoryRepository.save(category);
    }
}
