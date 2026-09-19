package com.community.controller.admin;

import com.community.common.PageVO;
import com.community.common.Result;
import com.community.dto.GoodsRejectDTO;
import com.community.dto.GoodsVO;
import com.community.service.GoodsService;
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
@RequestMapping("/admin/goods")
@RequiredArgsConstructor
public class AdminGoodsController {

    private final GoodsService goodsService;

    @GetMapping("/page")
    public Result<PageVO<GoodsVO>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(required = false) String keyword) {
        return Result.success(goodsService.pageAdmin(page, size, status, keyword));
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        goodsService.approve(id);
        return Result.success("审核通过", null);
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody GoodsRejectDTO dto) {
        goodsService.reject(id, dto.getReason());
        return Result.success("已驳回", null);
    }

    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        goodsService.forceOffShelf(id);
        return Result.success("已强制下架", null);
    }
}
