package com.eeit219.work_order_system.modules.e.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.e.repository.WorkOrderRepository;

@Service
public class ReportService {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    public List<CategoryReportDto> getCategoryReport() {
        return workOrderRepository.countWorkOrdersByCategory();	//呼叫方法
    }
}