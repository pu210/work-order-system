package com.eeit219.work_order_system.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.eeit219.work_order_system.common.websocket.NotificationWebSocketHandler;

/**
 * WebSocket 配置類別 (WebSocket Configuration)
 * 
 * 作用：啟用 WebSocket 功能並註冊端點 (Endpoint)，將 WebSocket 請求路由到對應的 Handler。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    // 透過建構子注入 Spring 管理的 NotificationWebSocketHandler 實例
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public WebSocketConfig(NotificationWebSocketHandler notificationWebSocketHandler) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    /**
     * 註冊 WebSocket Handler 與連線路徑 (Endpoint)
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 將連線路徑 "/ws/notifications" 綁定到 notificationWebSocketHandler 物件變數
        // setAllowedOrigins("*") 允許跨域連線 (CORS)
        registry.addHandler(notificationWebSocketHandler, "/ws/notifications")
                .setAllowedOriginPatterns("*");
    }
}