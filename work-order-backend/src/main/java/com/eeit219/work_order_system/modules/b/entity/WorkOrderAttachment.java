package com.eeit219.work_order_system.modules.b.entity;

import java.time.LocalDateTime;

import com.eeit219.work_order_system.modules.a.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_order_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Integer attachmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    // D模組新增，圖片關聯欄位：null=建立工單時上傳的附件；not null=留言上傳的附件。
    @Column(name = "contact_record_id")
    private Integer contactRecordId;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Integer fileSize;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_user_id", nullable = false)
    private User uploadedUser;
}
