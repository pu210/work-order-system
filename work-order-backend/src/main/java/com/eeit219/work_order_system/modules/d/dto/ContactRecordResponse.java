package com.eeit219.work_order_system.modules.d.dto;

import com.eeit219.work_order_system.modules.b.dto.WorkOrderAttachmentResponse;
import com.eeit219.work_order_system.modules.d.entity.ContactRecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ContactRecordResponse {

    // contact record
    private Integer recordId;
    private String content;
    private LocalDateTime createdTime;
    private ContactRecordType recordType;

    // work order
    private Integer workOrderId;

    // author
    private String authorUserName;
    private Integer authorUserId;
    private List<String> authorRoleCodes;

    // images uploaded with this contact record
    private List<WorkOrderAttachmentResponse> attachments;

}
