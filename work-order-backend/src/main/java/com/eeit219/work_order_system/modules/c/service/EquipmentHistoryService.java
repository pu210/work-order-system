package com.eeit219.work_order_system.modules.c.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.common.response.PageResponse;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;
import com.eeit219.work_order_system.modules.c.dto.EquipmentHistoryResponse;
import com.eeit219.work_order_system.modules.c.dto.EquipmentInfoResponse;
import com.eeit219.work_order_system.modules.c.dto.EquipmentWorkOrderListItemResponse;
import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.repository.RepairTicketHistoryRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.f.entity.RepairTarget;
import com.eeit219.work_order_system.modules.f.repository.RepairTargetRepository;

@Service
public class EquipmentHistoryService {

    private final RepairTargetRepository repairTargetRepository;
    private final RepairTicketHistoryRepository repairTicketHistoryRepository;

    public EquipmentHistoryService(
            RepairTargetRepository repairTargetRepository,
            RepairTicketHistoryRepository repairTicketHistoryRepository
    ) {
        this.repairTargetRepository = repairTargetRepository;
        this.repairTicketHistoryRepository = repairTicketHistoryRepository;
    }

    /**
     * 根據設備編號查詢設備資料以及歷史工單。
     */
    @Transactional(readOnly = true)
    public EquipmentHistoryResponse getHistory(
            String targetNo,
            String period,
            Pageable pageable
    ) {
        RepairTarget equipment = repairTargetRepository
                .findByTargetNo(targetNo)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "找不到設備：" + targetNo
                        )
                );

        LocalDateTime completedAfter = resolveCompletedAfter(period);

        Page<EquipmentWorkOrderListItemResponse> workOrderPage =
                repairTicketHistoryRepository
                        .findCompletedEquipmentHistory(
                                targetNo,
                                WorkOrderState.COMPLETED,
                                completedAfter,
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
            RepairTicketHistory history
    ) {
        WorkOrder workOrder = history.getWorkOrder();
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
                .completedTime(history.getEditedTime())
                .dueTime(workOrder.getDueTime())
                .isOverdue(workOrder.getIsOverdue())
                .build();
    }

    private LocalDateTime resolveCompletedAfter(String period) {
        String normalizedPeriod = period == null
                ? "ALL"
                : period.trim().toUpperCase(Locale.ROOT);
        LocalDateTime now = LocalDateTime.now();

        return switch (normalizedPeriod) {
            case "ALL" -> null;
            case "7D" -> now.minusDays(7);
            case "1M" -> now.minusMonths(1);
            case "3M" -> now.minusMonths(3);
            case "6M" -> now.minusMonths(6);
            case "1Y" -> now.minusYears(1);
            default -> throw new IllegalArgumentException(
                    "period 僅支援 ALL、7D、1M、3M、6M、1Y"
            );
        };
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
