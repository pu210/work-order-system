package com.eeit219.work_order_system.modules.e.dto;

public interface CategoryReportDto {
    String getCategoryName();

    String getSubCategoryName();

    String getStatusName();

    String getCreatorName();

    String getPriorityName();

    Long getCount();
}