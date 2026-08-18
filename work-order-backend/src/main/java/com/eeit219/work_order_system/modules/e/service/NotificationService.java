package com.eeit219.work_order_system.modules.e.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.entity.Notification;
import com.eeit219.work_order_system.modules.e.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification sendNotification(Integer receiverId, Integer senderId, Integer workOrderId, String title,
            String message, WorkOrderState status) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId); // 接收通知的人（如：報修人 ID）
        notification.setSenderId(senderId); // 發送通知的人（如：審核管理員 ID）
        notification.setWorkOrderId(workOrderId); // 對應的工單 ID
        notification.setTitle(title); // 通知標題（如："工單審核通過"）
        notification.setMessage(message); // 通知詳細內容
        notification.setStatus(status); // 當時的工單狀態
        notification.setIsRead(false); // 預設為未讀
        return notificationRepository.save(notification); // 存入資料庫
    }

    public List<Notification> getNotificationsByReceiverId(Integer receiverId) {
        return notificationRepository.findByReceiverIdOrderByNotificationIdDesc(receiverId);
    }
}