package com.eeit219.work_order_system.common.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.eeit219.work_order_system.common.security.JsonWebTokenUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // 支援單一使用者建立多個 WebSocket 連線 (例如同時開啟多分頁、多視窗或行動裝置)
    private static final Map<Integer, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final JsonWebTokenUtility jwtUtil;

    // 配置支援 Java 8 LocalDateTime 的 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 當前端連線成功時觸發 (Client 剛打開網頁或開新分頁)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
            System.out.println("✅ [JWT 驗證成功] 使用者 " + userId + " 建立 WebSocket 安全連線！(Session ID: " + session.getId() + ")");
        } else {
            System.out.println("⛔ [JWT 驗證失敗] WebSocket 連線拒絕：未提供有效的 Token 或 Token 已過期！Session ID: " + session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    // 當前端離線關閉網頁/分頁時觸發
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Integer userId = getUserIdFromSession(session);
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            System.out.println("❌ 使用者 " + userId + " 斷開 WebSocket 連線！(Session ID: " + session.getId() + ")");
        }
    }

    // 傳送通知給指定使用者的所有線上連線 (多分頁同步推播)
    public void sendNotificationToUser(Integer receiverId, Object notificationData) {
        Set<WebSocketSession> sessions = userSessions.get(receiverId);
        if (sessions != null && !sessions.isEmpty()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(notificationData);
                int sentCount = 0;
                for (WebSocketSession session : sessions) {
                    if (session != null && session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(jsonMessage));
                            sentCount++;
                        } catch (IOException e) {
                            System.err.println("推播訊息至 Session " + session.getId() + " 失敗: " + e.getMessage());
                        }
                    }
                }
                System.out.println("🚀 已成功推播即時通知給 User " + receiverId + " (" + sentCount + " 個 WebSocket 連線)");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ 無法推播即時通知：User " + receiverId + " 目前未建立 WebSocket 連線。在線 Users: " + userSessions.keySet());
        }
    }

    // 工具方法：從 Query 網址中抓取與解密 JWT Token 參數
    private Integer getUserIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null) {
                String token = null;
                // 優先從 ?token= 提取並驗證 JWT Token
                if (query.contains("token=")) {
                    token = query.split("token=")[1].split("&")[0];
                } else if (query.contains("userId=")) {
                    // 相容舊的 userId= 寫法
                    String userIdStr = query.split("userId=")[1].split("&")[0];
                    return Integer.parseInt(userIdStr);
                }

                if (token != null) {
                    String subject = jwtUtil.validateToken(token);
                    if (subject != null) {
                        JSONObject userData = new JSONObject(subject);
                        return userData.getInt("userId");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析 WebSocket Token 失敗: " + e.getMessage());
        }
        return null;
    }
}