package com.community.controller;

import com.community.common.PageVO;
import com.community.common.Result;
import com.community.dto.VisitorCreateDTO;
import com.community.entity.Visitor;
import com.community.interceptor.UserContext;
import com.community.service.VisitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @PostMapping
    public Result<Long> register(@Valid @RequestBody VisitorCreateDTO dto) {
        return Result.success("访客登记成功", visitorService.register(dto, UserContext.get().getId()));
    }

    @GetMapping("/page")
    public Result<PageVO<Visitor>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return Result.success(visitorService.pageMyVisitors(UserContext.get().getId(), page, size));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        visitorService.cancel(id, UserContext.get().getId());
        return Result.success("已取消登记", null);
    }
}
