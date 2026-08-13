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
    private Integer notificationId; // 通知編號（主鍵，自增）

    @Column(name = "work_order_id")
    private Integer workOrderId; // 工單編號（外鍵）

    @Column(name = "status", length = 20)
    private String status; // 工單狀態

    @Column(name = "title", length = 100)
    private String title; // 標題

    @Column(name = "message", columnDefinition = "NVARCHAR(500)")
    private String message; // 內容

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // 未讀/已讀狀態 (SQL: BIT, 0: 未讀, 1: 已讀)

    @Column(name = "sender_id")
    private Integer senderId; // 發送者 ID（外鍵）

    @Column(name = "receiver_id")
    private Integer receiverId; // 接收者 ID（外鍵）

    @Column(name = "priority_id")
    private Integer priorityId; // 優先程度（外鍵）

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime; // 建立時間

    // 手動補充 Getter / Setter，防止 IDE (Lombok) 對 isRead 欄位名稱產生判斷快取誤差
    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}