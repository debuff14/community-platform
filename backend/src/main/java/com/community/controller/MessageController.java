package com.community.controller;

import com.community.common.PageVO;
import com.community.common.Result;
import com.community.entity.Message;
import com.community.interceptor.UserContext;
import com.community.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/page")
    public Result<PageVO<Message>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return Result.success(messageService.page(UserContext.get().getId(), page, size));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount(UserContext.get().getId()));
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead(UserContext.get().getId());
        return Result.success("已全部标记为已读", null);
    }
}
