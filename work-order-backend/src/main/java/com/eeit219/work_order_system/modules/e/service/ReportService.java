package com.eeit219.work_order_system.modules.e.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.e.dto.CategoryReportDto;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.e.repository.ReportWorkOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportWorkOrderRepository workOrderRepository;

    public List<CategoryReportDto> getCategoryReport() {
        return workOrderRepository.countWorkOrdersByCategory();
    }

    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    public List<CategoryReportDto> getSubCategoryReport() {
        return workOrderRepository.countWorkOrdersBySubCategory();
    }
    // @Transactional
    // public WorkOrder createSampleWorkOrder() {
    // WorkOrder wo = new WorkOrder();
    // wo.setWorkOrderNo("WO-2026-" + (System.currentTimeMillis() % 10000));
    // wo.setTitle("測試冷氣報修工單");
    // wo.setSubCategoryId(1);
    // wo.setPriorityId(1);
    // wo.setLocationDetail("A棟 3樓");
    // wo.setCreatorUserId(1);
    // wo.setStatus("PENDING_REVIEW");
    // return workOrderRepository.save(wo);
    // }
}