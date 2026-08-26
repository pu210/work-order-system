package com.eeit219.work_order_system.modules.c.dto;

import com.eeit219.work_order_system.common.response.PageResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EquipmentHistoryResponse {

    private EquipmentInfoResponse equipment;

    private PageResponse<EquipmentWorkOrderListItemResponse> workOrders;
}