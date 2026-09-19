package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.PageVO;
import com.community.entity.Message;
import com.community.mapper.MessageMapper;
import com.community.service.MessageService;
import com.community.websocket.MessageWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final MessageWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    @Override
    public void push(Long userId, String content, Integer type, Long relatedId) {
        Message message = new Message();
        message.setUserId(userId);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setIsRead(0);
        messageMapper.insert(message);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "message");
        payload.put("content", content);
        payload.put("relatedId", relatedId);
        payload.put("unreadCount", unreadCount(userId));
        try {
            webSocketHandler.sendToUser(userId, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("站内消息实时推送失败，用户 {}", userId, e);
        }
    }

    @Override
    public PageVO<Message> page(Long userId, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 10;
        }
        Page<Message> dbPage = messageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getUserId, userId)
                        .orderByDesc(Message::getCreateTime)
                        .orderByDesc(Message::getId));
        return PageVO.of(dbPage);
    }

    @Override
    public long unreadCount(Long userId) {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
        return count == null ? 0 : count;
    }

    @Override
    public void markAllRead(Long userId) {
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));
    }
}
