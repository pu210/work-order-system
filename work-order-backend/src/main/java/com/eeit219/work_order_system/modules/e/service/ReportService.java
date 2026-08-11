package com.eeit219.work_order_system.modules.e.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.e.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final WorkOrderRepository workOrderRepository;

    public List<CategoryReportDto> getCategoryReport() {
        return workOrderRepository.countWorkOrdersByCategory();	//呼叫方法
    }
}