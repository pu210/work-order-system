package com.eeit219.work_order_system.modules.f.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<RepairCategory>>> getAllOrSearchCategories(
            @RequestParam(required = false) String keyword) {
        try {
            List<RepairCategory> list;
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = repairCategoryRepository.searchByKeyword(keyword);

                // 🌟 關鍵：如果帶有關鍵字卻查不到資料，主動拋出例外讓 catch 攔截
                if (list.isEmpty()) {
                    throw new RuntimeException("找不到符合條件的報修大類");
                }
            } else {
                list = repairCategoryRepository.findAll();
            }
            ApiResponse<List<RepairCategory>> response = ApiResponse.success(HttpStatus.OK.value(), "查詢報修大類成功", list);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            // 查不到資料時，轉成對應的失敗格式與狀態碼 (例如 401 或 400)
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RepairCategory>> createCategory(@RequestBody RepairCategory category) {
        try {
            category.setCreatedTime(LocalDateTime.now());
            category.setUpdatedTime(LocalDateTime.now());
            if (category.getStatus() == null) {
                category.setStatus(true);
            }
            RepairCategory saved = repairCategoryRepository.save(category);

            RepairCategory result = repairCategoryRepository.findById(saved.getRepairCategoriesId()).orElse(saved);
            ApiResponse<RepairCategory> response = ApiResponse.success(HttpStatus.CREATED.value(), "新增報修大類成功", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        }
    }

    @PutMapping("/{repairCategoriesId}")
    public ResponseEntity<ApiResponse<RepairCategory>> updateCategory(@PathVariable Integer repairCategoriesId,
            @RequestBody RepairCategory categoryDetails) {
        try {
            RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                    .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

            category.setName(categoryDetails.getName());
            category.setDefaultPriorityId(categoryDetails.getDefaultPriorityId());
            category.setStatus(categoryDetails.getStatus());
            category.setUpdatedTime(LocalDateTime.now());

            RepairCategory saved = repairCategoryRepository.save(category);

            RepairCategory result = repairCategoryRepository.findById(saved.getRepairCategoriesId()).orElse(saved);
            ApiResponse<RepairCategory> response = ApiResponse.success(HttpStatus.OK.value(), "更新報修大類成功", result);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PatchMapping("/{repairCategoriesId}/status")
    public ResponseEntity<ApiResponse<RepairCategory>> updateStatus(@PathVariable Integer repairCategoriesId,
            @RequestParam Boolean status) {
        try {
            RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                    .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

            category.setStatus(status);
            category.setUpdatedTime(LocalDateTime.now());

            RepairCategory saved = repairCategoryRepository.save(category);
            RepairCategory result = repairCategoryRepository.findById(saved.getRepairCategoriesId()).orElse(saved);
            ApiResponse<RepairCategory> response = ApiResponse.success(HttpStatus.OK.value(), "更新報修大類狀態成功", result);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    // 🛠️ 支援泛型的共用錯誤回應小工具
    private <T> ResponseEntity<ApiResponse<T>> errorResponse(int status, String message) {
        ApiResponse<T> response = ApiResponse.error(status, message);
        return ResponseEntity.status(status).body(response);
    }
}