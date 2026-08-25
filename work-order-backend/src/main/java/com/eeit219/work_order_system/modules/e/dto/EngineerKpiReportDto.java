package com.eeit219.work_order_system.modules.e.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngineerKpiReportDto {
    private Integer editorId;
    private String engineerName;
    private Long completedCount;
    private Double avgDurationHours;
    private Long avgDurationMinutes;
}
