package com.community.controller;

import com.community.common.PageVO;
import com.community.common.Result;
import com.community.dto.RepairCommentDTO;
import com.community.dto.RepairCreateDTO;
import com.community.dto.RepairOrderVO;
import com.community.entity.RepairOrder;
import com.community.interceptor.UserContext;
import com.community.service.RepairService;
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
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;

    @PostMapping
    public Result<Long> create(@Valid @RequestBody RepairCreateDTO dto) {
        return Result.success("报修提交成功", repairService.create(dto, UserContext.get().getId()));
    }

    @GetMapping("/page")
    public Result<PageVO<RepairOrder>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(repairService.pageForOwner(UserContext.get().getId(), page, size, status));
    }

    @GetMapping("/{id}")
    public Result<RepairOrderVO> detail(@PathVariable Long id) {
        return Result.success(repairService.getDetail(id, UserContext.get().getId(), false));
    }

    @PostMapping("/{id}/comment")
    public Result<Void> evaluate(@PathVariable Long id, @Valid @RequestBody RepairCommentDTO dto) {
        repairService.evaluate(id, dto, UserContext.get().getId());
        return Result.success("评价成功", null);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        repairService.cancel(id, UserContext.get().getId());
        return Result.success("已取消报修", null);
    }
}
