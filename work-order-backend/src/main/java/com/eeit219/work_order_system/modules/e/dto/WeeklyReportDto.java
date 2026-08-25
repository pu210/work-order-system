package com.eeit219.work_order_system.modules.e.dto;

public interface WeeklyReportDto {
    Integer getYear();

    Integer getMonth();

    Integer getWeek();

    Long getCount();
}
