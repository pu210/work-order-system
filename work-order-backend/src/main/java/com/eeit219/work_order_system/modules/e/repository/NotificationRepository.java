package com.eeit219.work_order_system.modules.e.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.e.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    // 繼承 JpaRepository 後，自動擁有 save()、findById()、findAll() 等基本 CRUD 功能
    List<Notification> findByReceiverIdOrderByNotificationIdDesc(Integer receiverId);
    
}