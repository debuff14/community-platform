package com.community.controller.admin;

import com.community.common.PageVO;
import com.community.common.Result;
import com.community.dto.RepairCompleteDTO;
import com.community.dto.RepairOrderVO;
import com.community.interceptor.UserContext;
import com.community.service.RepairService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/repair")
@RequiredArgsConstructor
public class AdminRepairController {

    private final RepairService repairService;

    @GetMapping("/page")
    public Result<PageVO<RepairOrderVO>> page(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) Integer type,
                                              @RequestParam(required = false) Integer urgency) {
        return Result.success(repairService.pageForAdmin(page, size, status, type, urgency));
    }

    @GetMapping("/{id}")
    public Result<RepairOrderVO> detail(@PathVariable Long id) {
        return Result.success(repairService.getDetail(id, UserContext.get().getId(), true));
    }

    @PutMapping("/{id}/accept")
    public Result<Void> accept(@PathVariable Long id) {
        repairService.accept(id);
        return Result.success("接单成功", null);
    }

    @PutMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        repairService.start(id);
        return Result.success("已开始处理", null);
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @Valid @RequestBody RepairCompleteDTO dto) {
        repairService.complete(id, dto.getResult());
        return Result.success("工单已完成", null);
    }
}
