package com.eeit219.work_order_system.modules.F.service; // ← 改成 service 包

import com.eeit219.work_order_system.modules.F.entity.SubCategory;
import com.eeit219.work_order_system.modules.F.dto.SubCategoryResponseDto;

import org.springframework.stereotype.Service;

@Service
public class SubCategoryService {

    public SubCategoryResponseDto convertToResponseDto(SubCategory subCategory) {
        if (subCategory == null) {
            return null;
        }
        SubCategoryResponseDto dto = new SubCategoryResponseDto();

        dto.setSubCategoriesId(subCategory.getSubCategoriesId());
        dto.setName(subCategory.getName());
        dto.setStatus(subCategory.getStatus());

        // 大類資訊：categoryId 直接用 subCategory 自己的欄位
        dto.setCategoryId(subCategory.getCategoryId());
        if (subCategory.getRepairCategory() != null) {
            dto.setCategoryName(subCategory.getRepairCategory().getName());
        }

        // 特例優先級
        if (subCategory.getOverridePriority() != null) {
            dto.setOverridePriorityId(subCategory.getOverridePriority().getPrioritiesId());
            dto.setOverridePriorityName(subCategory.getOverridePriority().getName());
        }

        // 核心計算：effectivePriorityId
        Integer effectiveId = subCategory.getOverridePriorityId() != null
                ? subCategory.getOverridePriorityId()
                : (subCategory.getRepairCategory() != null ? subCategory.getRepairCategory().getDefaultPriorityId()
                        : null);
        dto.setEffectivePriorityId(effectiveId);

        // 時間欄位
        dto.setCreatedAt(subCategory.getCreatedTime());
        dto.setUpdatedAt(subCategory.getUpdatedTime());

        return dto;
    }
}