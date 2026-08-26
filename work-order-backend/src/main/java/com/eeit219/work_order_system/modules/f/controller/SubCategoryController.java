package com.eeit219.work_order_system.modules.f.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
import com.eeit219.work_order_system.modules.f.dto.SubCategoryRequestDto;
import com.eeit219.work_order_system.modules.f.dto.SubCategoryResponseDto;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.entity.RepairCategory;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;
import com.eeit219.work_order_system.modules.f.repository.RepairCategoryRepository;
import com.eeit219.work_order_system.modules.f.repository.SubCategoryRepository;
import com.eeit219.work_order_system.modules.f.service.SubCategoryService;

@RestController
@RequestMapping("/api/sub-categories")
public class SubCategoryController {

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private RepairCategoryRepository repairCategoryRepository;

    @Autowired
    private PriorityRepository priorityRepository; // 用來查優先級中文名稱

    @Autowired
    private SubCategoryService subCategoryService;

    // 取得所有資料，或透過 keyword 進行模糊搜尋
    @GetMapping
    public List<SubCategoryResponseDto> getAllSubCategories(
            @RequestParam(required = false) String keyword) {

        List<SubCategory> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = subCategoryRepository.searchByKeyword(keyword);
        } else {
            list = subCategoryRepository.findAll();
        }

        return list.stream()
                .map(subCategoryService::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // B 模組用：新增工單頁的細項下拉選單，只回傳啟用中（status = true）的資料。
    // 獨立一支端點，跟上面給系統設定頁用的 getAllSubCategories() 分開，F 模組調整那支時不會影響到這裡。
    @GetMapping("/active")
    public ApiResponse<List<SubCategoryResponseDto>> getActiveSubCategories() {
        List<SubCategoryResponseDto> data = subCategoryRepository.findByStatusTrue().stream()
                .map(subCategoryService::convertToResponseDto)
                .collect(Collectors.toList());
        return ApiResponse.success(HttpStatus.OK.value(), "成功", data);
    }

    @PostMapping
    public SubCategory createSubCategory(@RequestBody SubCategoryRequestDto request) {
        SubCategory sub = new SubCategory();
        sub.setCategoryId(request.getCategoryId());
        sub.setName(request.getName());
        sub.setOverridePriorityId(request.getOverridePriorityId());

        // 自動根據 overridePriorityId 填入對應的中文名稱
        if (request.getOverridePriorityId() != null) {
            Priority priority = priorityRepository.findById(request.getOverridePriorityId()).orElse(null);
            if (priority != null) {
                sub.setOverridePriorityName(priority.getName());
            }
        }

        sub.setStatus(request.getStatus() != null ? request.getStatus() : true);
        sub.setCreatedTime(LocalDateTime.now());
        sub.setUpdatedTime(LocalDateTime.now());

        return subCategoryRepository.save(sub);
    }

    @PutMapping("/{subCategoriesId}")
    public SubCategory updateSubCategory(@PathVariable Integer subCategoriesId,
            @RequestBody SubCategoryRequestDto request) {
        SubCategory sub = subCategoryRepository.findById(subCategoriesId)
                .orElseThrow(() -> new RuntimeException("找不到該報修細分 ID: " + subCategoriesId));

        sub.setCategoryId(request.getCategoryId());
        sub.setName(request.getName());
        sub.setOverridePriorityId(request.getOverridePriorityId());

        // 更新時同步更新對應的中文名稱
        if (request.getOverridePriorityId() != null) {
            Priority priority = priorityRepository.findById(request.getOverridePriorityId()).orElse(null);
            if (priority != null) {
                sub.setOverridePriorityName(priority.getName());
            }
        } else {
            sub.setOverridePriorityName(null);
        }

        // 🌟 加上防呆：有傳 status 才更新，沒傳就維持原樣
        if (request.getStatus() != null) {
            sub.setStatus(request.getStatus());
        }

        sub.setUpdatedTime(LocalDateTime.now());

        return subCategoryRepository.save(sub);
    }

    @PatchMapping("/{subCategoriesId}/status")
    public SubCategory updateStatus(@PathVariable Integer subCategoriesId, @RequestParam Boolean status) {
        SubCategory sub = subCategoryRepository.findById(subCategoriesId)
                .orElseThrow(() -> new RuntimeException("找不到該報修細分 ID: " + subCategoriesId));

        sub.setStatus(status);
        sub.setUpdatedTime(LocalDateTime.now());

        return subCategoryRepository.save(sub);
    }
}
