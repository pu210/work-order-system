package com.eeit219.work_order_system.modules.b.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class WorkOrderListItemResponse extends WorkOrderSummary {
    private String assignedHandlerName;
}
