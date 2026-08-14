package com.eeit219.work_order_system.modules.f.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.f.dto.SubCategoryRequestDto;
import com.eeit219.work_order_system.modules.f.dto.SubCategoryResponseDto;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;
import com.eeit219.work_order_system.modules.f.repository.RepairCategoryRepository;
import com.eeit219.work_order_system.modules.f.repository.SubCategoryRepository;

@Service
public class SubCategoryService {

    @Autowired
    private SubCategoryRepository subCategoryRepository;
    @Autowired
    private RepairCategoryRepository repairCategoryRepository;
    @Autowired
    private PriorityRepository priorityRepository;

    public SubCategoryResponseDto convertToResponseDto(SubCategory sub) {
        if (sub == null) {
            return null;
        }

        SubCategoryResponseDto dto = new SubCategoryResponseDto();
        dto.setSubCategoriesId(sub.getSubCategoriesId());
        dto.setName(sub.getName());
        dto.setStatus(sub.getStatus());
        dto.setCreatedTime(sub.getCreatedTime());
        dto.setUpdatedTime(sub.getUpdatedTime());

        // 設定大類資訊
        if (sub.getRepairCategory() != null) {
            dto.setCategoryId(sub.getRepairCategory().getRepairCategoriesId());
            dto.setCategoryName(sub.getRepairCategory().getName());
        }

        // 設定特例與生效優先級
        if (sub.getOverridePriority() != null) {
            dto.setOverridePriorityId(sub.getOverridePriority().getPrioritiesId());
            dto.setOverridePriorityName(sub.getOverridePriority().getName());
            dto.setEffectivePriorityId(sub.getOverridePriority().getPrioritiesId());
            dto.setEffectivePriorityName(sub.getOverridePriority().getName());
        } else if (sub.getRepairCategory() != null && sub.getRepairCategory().getDefaultPriority() != null) {
            dto.setEffectivePriorityId(sub.getRepairCategory().getDefaultPriority().getPrioritiesId());
            dto.setEffectivePriorityName(sub.getRepairCategory().getDefaultPriority().getName());
        }
        return dto;
    }

    public SubCategory createSubCategory(SubCategoryRequestDto req) {
        SubCategory sub = new SubCategory();
        sub.setName(req.getName());
        sub.setStatus(req.getStatus() != null ? req.getStatus() : true);
        sub.setCreatedTime(LocalDateTime.now());
        sub.setUpdatedTime(LocalDateTime.now());

        if (req.getCategoryId() != null) {
            sub.setRepairCategory(repairCategoryRepository.findById(req.getCategoryId()).orElse(null));
        }
        if (req.getOverridePriorityId() != null) {
            sub.setOverridePriority(priorityRepository.findById(req.getOverridePriorityId()).orElse(null));
        }
        return subCategoryRepository.save(sub);
    }
}
