package com.eeit219.work_order_system.modules.f.service; // ← 改成 service 包

import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.f.dto.SubCategoryResponseDto;
import com.eeit219.work_order_system.modules.f.entity.SubCategory;

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

        // 2. 處理 overridePriorityId 與 overridePriorityName
        // 如果子類有填，就用子類的；如果沒填，就讓它等於 effectiveId，並抓大類的預設名稱
        if (subCategory.getOverridePriority() != null) {
            dto.setOverridePriorityId(subCategory.getOverridePriority().getPrioritiesId());
            dto.setOverridePriorityName(subCategory.getOverridePriority().getName());
        } else if (subCategory.getRepairCategory() != null) {
            // 如果子類沒填，這裡把大類的預設 ID 與名稱補上去，這樣模糊比對就不會是空值了
            dto.setOverridePriorityId(subCategory.getRepairCategory().getDefaultPriorityId());
            dto.setOverridePriorityName(subCategory.getRepairCategory().getDefaultPriorityName());
        }

        // 時間欄位
        dto.setCreated_time(subCategory.getCreatedTime());
        dto.setUpdated_time(subCategory.getUpdatedTime());

        return dto;
    }
}
