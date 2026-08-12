package com.eeit219.work_order_system.modules.b.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class WorkOrderAttachmentResponse {
    private Integer attachmentId;
    private String originalFileName;
    private String contentType;
    private Integer fileSize;
    private LocalDateTime createdTime;
    private String uploadedUserName;
}
