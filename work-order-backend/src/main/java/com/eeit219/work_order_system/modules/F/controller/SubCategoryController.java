package com.eeit219.work_order_system.modules.F.controller;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.F.service.SubCategoryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

import com.eeit219.work_order_system.modules.F.dto.SubCategoryRequestDto;
import com.eeit219.work_order_system.modules.F.dto.SubCategoryResponseDto;
import com.eeit219.work_order_system.modules.F.entity.Priority;
import com.eeit219.work_order_system.modules.F.entity.SubCategory;
import com.eeit219.work_order_system.modules.F.repository.PriorityRepository;
import com.eeit219.work_order_system.modules.F.repository.RepairCategoryRepository;
import com.eeit219.work_order_system.modules.F.repository.SubCategoryRepository;

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
    public ResponseEntity<ApiResponse<List<SubCategoryResponseDto>>> getAllSubCategories(
            @RequestParam(required = false) String keyword) {
        try {
            List<SubCategory> list;
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = subCategoryRepository.searchByKeyword(keyword);

                // 轉換成 DTO
                List<SubCategoryResponseDto> dtoList = list.stream()
                        .map(subCategoryService::convertToResponseDto)
                        .collect(Collectors.toList());

                // 🌟 關鍵：如果帶有關鍵字卻查不到資料，主動拋出例外讓 catch 攔截
                if (dtoList.isEmpty()) {
                    throw new RuntimeException("找不到符合條件的報修細分");
                }

                ApiResponse<List<SubCategoryResponseDto>> response = ApiResponse.success(HttpStatus.OK.value(),
                        "查詢報修細分成功", dtoList);
                return ResponseEntity.ok(response);
            } else {
                list = subCategoryRepository.findAll();
                List<SubCategoryResponseDto> dtoList = list.stream()
                        .map(subCategoryService::convertToResponseDto)
                        .collect(Collectors.toList());

                ApiResponse<List<SubCategoryResponseDto>> response = ApiResponse.success(HttpStatus.OK.value(),
                        "查詢報修細分成功", dtoList);
                return ResponseEntity.ok(response);
            }
        } catch (RuntimeException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubCategory>> createSubCategory(@RequestBody SubCategoryRequestDto request) {
        try {
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

            SubCategory saved = subCategoryRepository.save(sub);
            ApiResponse<SubCategory> response = ApiResponse.success(HttpStatus.CREATED.value(), "新增報修細分成功", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        }
    }

    @PutMapping("/{subCategoriesId}")
    public ResponseEntity<ApiResponse<SubCategory>> updateSubCategory(@PathVariable Integer subCategoriesId,
            @RequestBody SubCategoryRequestDto request) {
        try {
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

            sub.setStatus(request.getStatus());
            sub.setUpdatedTime(LocalDateTime.now());

            SubCategory updated = subCategoryRepository.save(sub);
            ApiResponse<SubCategory> response = ApiResponse.success(HttpStatus.OK.value(), "更新報修細分成功", updated);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PatchMapping("/{subCategoriesId}/status")
    public ResponseEntity<ApiResponse<SubCategory>> updateStatus(@PathVariable Integer subCategoriesId,
            @RequestParam Boolean status) {
        try {
            SubCategory sub = subCategoryRepository.findById(subCategoriesId)
                    .orElseThrow(() -> new RuntimeException("找不到該報修細分 ID: " + subCategoriesId));

            sub.setStatus(status);
            sub.setUpdatedTime(LocalDateTime.now());

            SubCategory updated = subCategoryRepository.save(sub);
            ApiResponse<SubCategory> response = ApiResponse.success(HttpStatus.OK.value(), "更新報修細分狀態成功", updated);
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