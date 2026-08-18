package com.eeit219.work_order_system.modules.d.dto;

import com.eeit219.work_order_system.modules.d.entity.ContactRecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ContactRecordResponse {

    // contact record
    private Integer recordId;
    private String content;
    private LocalDateTime createdTime;

    private ContactRecordType recordType;


    // author
    private Integer workOrderId;
    private String authorUserName;
    private Integer authorUserId;

}
