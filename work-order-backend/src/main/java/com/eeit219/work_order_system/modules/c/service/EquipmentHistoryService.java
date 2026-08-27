package com.eeit219.work_order_system.modules.c.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.common.response.PageResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.dto.EquipmentHistoryResponse;
import com.eeit219.work_order_system.modules.c.dto.EquipmentInfoResponse;
import com.eeit219.work_order_system.modules.c.dto.EquipmentWorkOrderListItemResponse;
import com.eeit219.work_order_system.modules.f.entity.RepairTarget;
import com.eeit219.work_order_system.modules.f.repository.RepairTargetRepository;

@Service
public class EquipmentHistoryService {

    private final RepairTargetRepository repairTargetRepository;
    private final WorkOrderRepository workOrderRepository;

    public EquipmentHistoryService(
            RepairTargetRepository repairTargetRepository,
            WorkOrderRepository workOrderRepository
    ) {
        this.repairTargetRepository = repairTargetRepository;
        this.workOrderRepository = workOrderRepository;
    }

    /**
     * 根據設備編號查詢設備資料以及歷史工單。
     */
    @Transactional(readOnly = true)
    public EquipmentHistoryResponse getHistory(
            String targetNo,
            Pageable pageable
    ) {
        RepairTarget equipment = repairTargetRepository
                .findByTargetNo(targetNo)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "找不到設備：" + targetNo
                        )
                );

        Page<EquipmentWorkOrderListItemResponse> workOrderPage =
                workOrderRepository
                        .findByRepairTarget_TargetNoOrderByCreatedTimeDesc(
                                targetNo,
                                pageable
                        )
                        .map(this::toWorkOrderListItem);

        return EquipmentHistoryResponse.builder()
                .equipment(toEquipmentInfo(equipment))
                .workOrders(PageResponse.from(workOrderPage))
                .build();
    }

    /**
     * 將設備 Entity 轉換成設備資訊 DTO。
     */
    private EquipmentInfoResponse toEquipmentInfo(
            RepairTarget equipment
    ) {
        return EquipmentInfoResponse.builder()
                .targetId(equipment.getTargetId())
                .targetNo(equipment.getTargetNo())
                .name(equipment.getName())
                .model(equipment.getModel())
                .status(equipment.getStatus())
                .build();
    }

    /**
     * 將工單 Entity 轉換成歷史列表的一列。
     */
    private EquipmentWorkOrderListItemResponse toWorkOrderListItem(
            WorkOrder workOrder
    ) {
        return EquipmentWorkOrderListItemResponse.builder()
                .workOrderId(workOrder.getWorkOrderId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .title(workOrder.getTitle())
                .categoryName(getCategoryName(workOrder))
                .priorityName(getPriorityName(workOrder))
                .status(
                        workOrder.getStatus() == null
                                ? null
                                : workOrder.getStatus().name()
                )
                .creatorName(
                        workOrder.getCreator() == null
                                ? null
                                : workOrder.getCreator().getName()
                )
                .assignedHandlerName(
                        workOrder.getAssignedHandler() == null
                                ? null
                                : workOrder.getAssignedHandler().getName()
                )
                .createdTime(workOrder.getCreatedTime())
                .dueTime(workOrder.getDueTime())
                .isOverdue(workOrder.getIsOverdue())
                .build();
    }

    private String getCategoryName(WorkOrder workOrder) {
        if (workOrder.getSubCategory() == null
                || workOrder.getSubCategory().getRepairCategory() == null) {
            return null;
        }

        return workOrder.getSubCategory()
                .getRepairCategory()
                .getName();
    }

    private String getPriorityName(WorkOrder workOrder) {
        if (workOrder.getPriority() == null) {
            return null;
        }

        return workOrder.getPriority().getName();
    }
}