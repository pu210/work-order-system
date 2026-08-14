package com.eeit219.work_order_system.modules.e.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.security.AuthenticatedUser;
import com.eeit219.work_order_system.modules.e.entity.Notification;
import com.eeit219.work_order_system.modules.e.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin // 允許前端跨域測試
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 取得當前登入使用者的專屬通知
    // GET http://localhost:8080/api/notifications/my
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications(
            @AuthenticationPrincipal AuthenticatedUser loginUser) {

        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未提供驗證 Token 或尚未登入，請先登入系統"));
        }

        List<Notification> list = notificationService.getNotificationsByReceiverId(loginUser.userId());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢成功", list));
    }

    @GetMapping("/user/{receiverId}")
    public List<Notification> getNotificationsByUser(@PathVariable Integer receiverId) {
        return notificationService.getNotificationsByReceiverId(receiverId);
    }
}