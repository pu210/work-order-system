package com.eeit219.work_order_system.modules.e.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.common.websocket.NotificationWebSocketHandler;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.b.repository.WorkOrderRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.entity.Notification;
import com.eeit219.work_order_system.modules.e.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;

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
        // 1. 先存入資料庫，拿到包含流水號 ID 的成果 saved
        Notification saved = notificationRepository.save(notification);
        populateExtraInfo(saved);
        // 2. 拿這筆 saved 資料去發送 WebSocket 實時推播
        webSocketHandler.sendNotificationToUser(receiverId, saved);
        // 3. 最後把 saved 成果 return 交出去！
        return saved;
    }

    // 查詢使用者專屬通知列表
    public List<Notification> getNotificationsByReceiverId(Integer receiverId) {
        List<Notification> list = notificationRepository.findByReceiverIdOrderByNotificationIdDesc(receiverId);
        list.forEach(this::populateExtraInfo);
        return list;
    }

    @Transactional
    public Notification markAsRead(Integer notificationId){
        Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow
        (() -> new RuntimeException("通知不存在"+notificationId));
        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        populateExtraInfo(saved);
        return saved;
    }

    @Transactional
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private void populateExtraInfo(Notification notification) {
        if (notification == null) return;

        if (notification.getSenderId() != null) {
            userRepository.findById(notification.getSenderId())
                    .ifPresent(user -> notification.setSenderName(user.getName() != null && !user.getName().isBlank() ? user.getName() : user.getAccount()));
        } else {
            notification.setSenderName("系統");
        }

        if (notification.getWorkOrderId() != null) {
            workOrderRepository.findById(notification.getWorkOrderId())
                    .ifPresent(wo -> notification.setWorkOrderNo(wo.getWorkOrderNo()));
        }
    }
}                                                                                                                                                                                                                                             