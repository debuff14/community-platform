package com.community.controller.admin;

import com.community.common.PageVO;
import com.community.common.Result;
import com.community.dto.VisitorVO;
import com.community.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/visitor")
@RequiredArgsConstructor
public class AdminVisitorController {

    private final VisitorService visitorService;

    @GetMapping("/page")
    public Result<PageVO<VisitorVO>> page(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(visitorService.pageAdmin(page, size));
    }

    @PutMapping("/{id}/enter")
    public Result<Void> enter(@PathVariable Long id) {
        visitorService.enter(id);
        return Result.success("已登记入场", null);
    }

    @PutMapping("/{id}/leave")
    public Result<Void> leave(@PathVariable Long id) {
        visitorService.leave(id);
        return Result.success("已登记离场", null);
    }
}
