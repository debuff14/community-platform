package com.community.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = userIdOf(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("WebSocket 连接建立，用户 {}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = userIdOf(session);
        if (userId != null) {
            SESSIONS.remove(userId, session);
            log.info("WebSocket 连接关闭，用户 {}", userId);
        }
    }

    public void sendToUser(Long userId, String text) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            log.warn("WebSocket 推送失败，用户 {}", userId, e);
        }
    }

    private Long userIdOf(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId instanceof Long ? (Long) userId : null;
    }
}
