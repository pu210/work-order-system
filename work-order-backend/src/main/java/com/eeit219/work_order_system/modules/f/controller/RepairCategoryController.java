package com.eeit219.work_order_system.modules.f.controller;

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

import com.eeit219.work_order_system.common.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<RepairCategoryResponseDto>>> getAllOrSearchCategories(
            @RequestParam(required = false) String keyword) {
        try {
            List<RepairCategory> list;
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = repairCategoryRepository.searchByKeyword(keyword);

                if (list.isEmpty()) {
                    throw new RuntimeException("找不到符合條件的報修大類");
                }
            } else {
                list = repairCategoryRepository.findAll();
            }

            // 轉換成 ResponseDto 列表
            List<RepairCategoryResponseDto> dtoList = list.stream()
                    .map(repairCategoryService::convertToResponseDto)
                    .collect(Collectors.toList());

            ApiResponse<List<RepairCategoryResponseDto>> response = ApiResponse.success(HttpStatus.OK.value(), "查詢報修大類成功", dtoList);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RepairCategoryResponseDto>> createCategory(@RequestBody RepairCategoryRequestDto request) {
        try {
            RepairCategory saved = repairCategoryService.createCategory(request);
            RepairCategoryResponseDto responseDto = repairCategoryService.convertToResponseDto(saved);

            ApiResponse<RepairCategoryResponseDto> response = ApiResponse.success(HttpStatus.CREATED.value(), "新增報修大類成功", responseDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        }
    }

    @PutMapping("/{repairCategoriesId}")
    public ResponseEntity<ApiResponse<RepairCategoryResponseDto>> updateCategory(@PathVariable Integer repairCategoriesId,
            @RequestBody RepairCategoryRequestDto request) {
        try {
            RepairCategory updated = repairCategoryService.updateCategory(repairCategoriesId, request);
            RepairCategoryResponseDto responseDto = repairCategoryService.convertToResponseDto(updated);

            ApiResponse<RepairCategoryResponseDto> response = ApiResponse.success(HttpStatus.OK.value(), "更新報修大類成功", responseDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PatchMapping("/{repairCategoriesId}/status")
    public ResponseEntity<ApiResponse<RepairCategoryResponseDto>> updateStatus(@PathVariable Integer repairCategoriesId,
            @RequestParam Boolean status) {
        try {
            RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                    .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

            category.setStatus(status);
            category.setUpdatedTime(LocalDateTime.now());
            RepairCategory saved = repairCategoryRepository.save(category);

            RepairCategoryResponseDto responseDto = repairCategoryService.convertToResponseDto(saved);

            ApiResponse<RepairCategoryResponseDto> response = ApiResponse.success(HttpStatus.OK.value(), "更新報修大類狀態成功", responseDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> errorResponse(int status, String message) {
        ApiResponse<T> response = ApiResponse.error(status, message);
        return ResponseEntity.status(status).body(response);
    }
}
