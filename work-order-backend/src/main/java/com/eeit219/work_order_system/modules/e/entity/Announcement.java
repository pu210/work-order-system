package com.eeit219.work_order_system.modules.e.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "system_announcements", indexes = {
        @Index(name = "idx_announcements_is_pinned", columnList = "is_pinned"),
        @Index(name = "idx_announcements_created_time", columnList = "created_time")
})
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Integer announcementId; // 公告流水號（主鍵，自增，INT）

    @Column(name = "title", nullable = false, length = 150)
    private String title; // 公告標題 (NVARCHAR(150))

    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content; // 公告內容 (可支援 HTML / 富文本)

    @Column(name = "category", nullable = false, length = 30)
    private String category = "GENERAL"; // 公告類型（如：MAINTENANCE 維護、FEATURE 新功能、URGENT 緊急通知，VARCHAR(30)）

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false; // 是否置頂（SQL: BIT, true=置頂, false=不置頂）

    @Column(name = "start_time")
    private LocalDateTime startTime; // 公告生效時間（用於預約排程發布，DATETIME2）

    @Column(name = "end_time")
    private LocalDateTime endTime; // 公告下架時間（過期自動隱藏，DATETIME2）

    @Column(name = "created_by", nullable = false)
    private Integer createdBy; // 發布者 ID（管理者 ID，FK，INT）

    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime; // 建立時間 (預設 GETDATE()，DATETIME2)
}
