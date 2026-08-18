package com.eeit219.work_order_system.modules.f.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.f.dto.RepairCategoryRequestDto;
import com.eeit219.work_order_system.modules.f.dto.RepairCategoryResponseDto;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.entity.RepairCategory;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;
import com.eeit219.work_order_system.modules.f.repository.RepairCategoryRepository;

@Service
public class RepairCategoryService {

    @Autowired
    private RepairCategoryRepository repairCategoryRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    // Entity 轉 ResponseDto
    public RepairCategoryResponseDto convertToResponseDto(RepairCategory category) {
        if (category == null) {
            return null;
        }

        RepairCategoryResponseDto dto = new RepairCategoryResponseDto();
        dto.setRepairCategoriesId(category.getRepairCategoriesId());
        dto.setName(category.getName());
        dto.setStatus(category.getStatus());
        dto.setCreatedTime(category.getCreatedTime());
        dto.setUpdatedTime(category.getUpdatedTime());

        // 帶出預設優先級的 ID 與名稱
        if (category.getDefaultPriority() != null) {
            dto.setDefaultPriorityId(category.getDefaultPriority().getPrioritiesId());
            dto.setDefaultPriorityName(category.getDefaultPriority().getName());
        }

        return dto;
    }

    // 新增商業邏輯
    public RepairCategory createCategory(RepairCategoryRequestDto request) {
        RepairCategory category = new RepairCategory();
        category.setName(request.getName());
        category.setStatus(request.getStatus() != null ? request.getStatus() : true);
        category.setCreatedTime(LocalDateTime.now());
        category.setUpdatedTime(LocalDateTime.now());

        if (request.getDefaultPriorityId() != null) {
            Priority priority = priorityRepository.findById(request.getDefaultPriorityId())
                    .orElseThrow(() -> new RuntimeException("找不到指定的預設優先級 ID: " + request.getDefaultPriorityId()));
            category.setDefaultPriority(priority);
        }

        return repairCategoryRepository.save(category);
    }

    // 修改商業邏輯
    public RepairCategory updateCategory(Integer repairCategoriesId, RepairCategoryRequestDto request) {
        RepairCategory category = repairCategoryRepository.findById(repairCategoriesId)
                .orElseThrow(() -> new RuntimeException("找不到該報修大類 ID: " + repairCategoriesId));

        category.setName(request.getName());
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        category.setUpdatedTime(LocalDateTime.now());

        if (request.getDefaultPriorityId() != null) {
            Priority priority = priorityRepository.findById(request.getDefaultPriorityId())
                    .orElseThrow(() -> new RuntimeException("找不到指定的預設優先級 ID: " + request.getDefaultPriorityId()));
            category.setDefaultPriority(priority);
        }

        return repairCategoryRepository.save(category);
    }
}
