package com.eeit219.work_order_system.modules.e.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.e.entity.Notification;
import com.eeit219.work_order_system.modules.e.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createTestNotification(Notification notification) {
        // 設定預設值（如果前端沒傳的話）
        if (notification.getIsRead() == null) {
            notification.setIsRead(false); // 預設未讀
        }
        
        // 直接存入 DB 並回傳存好的物件（包含自動產生的 notification_id）
        return notificationRepository.save(notification);
    }
    
    public List<Notification> getNotificationsByReceiverId(Integer receiverId) {
        return notificationRepository.findByReceiverIdOrderByNotificationIdDesc(receiverId);
    }
}