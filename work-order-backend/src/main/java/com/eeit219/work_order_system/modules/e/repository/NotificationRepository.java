package com.eeit219.work_order_system.modules.e.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeit219.work_order_system.modules.e.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 繼承 JpaRepository 後，自動擁有 save()、findById()、findAll() 等基本 CRUD 功能
	List<Notification> findByReceiverIdOrderByNotificationIdDesc(Long receiverId);
}