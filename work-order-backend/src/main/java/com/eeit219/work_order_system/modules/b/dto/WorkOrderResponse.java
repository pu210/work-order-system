package com.eeit219.work_order_system.modules.b.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class WorkOrderResponse extends WorkOrderSummary {
    private String subCategoryName;
    private String locationDetail;
    private String contactPhone;
    private String description;
    private LocalDateTime dueTime;
    private List<WorkOrderAttachmentResponse> attachments;
}
