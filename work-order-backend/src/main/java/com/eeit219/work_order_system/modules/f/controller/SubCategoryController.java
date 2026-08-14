package com.eeit219.work_order_system.modules.f.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.f.dto.SubCategoryRequestDto;
import com.eeit219.work_order_system.modules.f.dto.SubCategoryResponseDto;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;
import com.eeit219.work_order_system.modules.f.repository.SubCategoryRepository;
import com.eeit219.work_order_system.modules.f.service.SubCategoryService;

@RestController
@RequestMapping("/api/sub-categories")
public class SubCategoryController {

    @Autowired
    private SubCategoryRepository subCategoryRepository;
    @Autowired
    private SubCategoryService subCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubCategoryResponseDto>>> getAll(@RequestParam(required = false) String keyword) {
        List<SubCategory> list = (keyword != null) ? subCategoryRepository.searchByKeyword(keyword) : subCategoryRepository.findAll();

        List<SubCategoryResponseDto> dtoList = list.stream()
                .map(subCategoryService::convertToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢成功", dtoList));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubCategoryResponseDto>> create(@RequestBody SubCategoryRequestDto request) {
        SubCategory saved = subCategoryService.createSubCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "新增成功", subCategoryService.convertToResponseDto(saved)));
    }
}
