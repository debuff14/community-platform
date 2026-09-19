package com.community.service;

import com.community.common.PageVO;
import com.community.entity.Message;

public interface MessageService {

    void push(Long userId, String content, Integer type, Long relatedId);

    PageVO<Message> page(Long userId, int page, int size);

    long unreadCount(Long userId);

    void markAllRead(Long userId);
}
