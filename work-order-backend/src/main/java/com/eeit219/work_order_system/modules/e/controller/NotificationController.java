package com.eeit219.work_order_system.modules.e.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.modules.e.entity.Notification;
import com.eeit219.work_order_system.modules.e.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin // 允許前端跨域測試
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 測試用 API：直接新增一筆通知
    // POST http://localhost:8080/api/notifications/test
    @PostMapping("/test")
    public Notification createNotificationTest(@RequestBody Notification notification) {
        
        return notificationService.createTestNotification(notification);
    }
    
    @GetMapping("/user/{receiverId}")
    public List<Notification> getNotificationsByUser(@PathVariable Long receiverId) {
        return notificationService.getNotificationsByReceiverId(receiverId);
    }
}