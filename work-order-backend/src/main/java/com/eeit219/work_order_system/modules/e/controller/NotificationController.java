package com.eeit219.work_order_system.modules.e.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.security.AuthenticatedUser;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;
import com.eeit219.work_order_system.modules.e.entity.Notification;
import com.eeit219.work_order_system.modules.e.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin // 允許前端跨域測試
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 取得當前登入使用者的專屬通知
    // GET http://localhost:8080/api/notifications/my
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未提供驗證 Token 或尚未登入，請先登入系統"));
        }

        Integer userId = null;
        if (auth.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
            userId = authenticatedUser.userId();
        } else {
            User user = userRepository.findByAccount(auth.getName()).orElse(null);
            if (user != null) {
                userId = user.getUserId();
            }
        }

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "找不到登入使用者資訊"));
        }

        List<Notification> list = notificationService.getNotificationsByReceiverId(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢成功", list));
    }

    @GetMapping("/user/{receiverId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotificationsByUser(@PathVariable Integer receiverId) {
        List<Notification> list = notificationService.getNotificationsByReceiverId(receiverId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢使用者通知成功", list));
    }

    @PatchMapping("/read/{notificationId}")
    public ResponseEntity<ApiResponse<Notification>> setNotificationsByRead(@PathVariable Integer notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "已標示為已讀", notification));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Integer notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "通知已從資料庫移除", null));
    }

    // 測試用發送通知 API (POST http://localhost:8080/api/notifications/test-send)
    @PostMapping("/test-send")
    public ResponseEntity<ApiResponse<Notification>> testSendNotification(
            @RequestParam Integer receiverId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(required = false) Integer workOrderId) {
        try {
            Notification notification = notificationService.sendNotification(
                    receiverId, 1, workOrderId, title, message, WorkOrderState.IN_PROGRESS);
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "發送成功", notification));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "發送失敗：" + e.getMessage()));
        }
    }
}