package com.eeit219.work_order_system.modules.e.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @Column(name = "work_order_id")
    private Integer workOrderId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "message", columnDefinition = "NVARCHAR(500)")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // 預設未讀 (0: 未讀, 1: 已讀)

    @Column(name = "sender_id")
    private Integer senderId;

    @Column(name = "receiver_id", nullable = false)
    private Integer receiverId;

    @Column(name = "priority_id")
    private Integer priorityId;

    // @CreationTimestamp
    // @Column(name = "created_time", updatable = false)
    // private LocalDateTime createdTime;
}