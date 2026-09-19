package com.community.controller;

import com.community.common.PageVO;
import com.community.common.Result;
import com.community.dto.GoodsSaveDTO;
import com.community.dto.GoodsVO;
import com.community.entity.Goods;
import com.community.interceptor.UserContext;
import com.community.service.GoodsService;
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
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @PostMapping
    public Result<Long> publish(@Valid @RequestBody GoodsSaveDTO dto) {
        return Result.success("发布成功，等待管理员审核", goodsService.publish(dto, UserContext.get().getId()));
    }

    @GetMapping("/my/page")
    public Result<PageVO<Goods>> pageMyGoods(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(goodsService.pageMyGoods(UserContext.get().getId(), page, size, status));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody GoodsSaveDTO dto) {
        goodsService.update(id, dto, UserContext.get().getId());
        return Result.success("修改成功，已重新提交审核", null);
    }

    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(@PathVariable Long id) {
        goodsService.offShelf(id, UserContext.get().getId());
        return Result.success("已下架", null);
    }

    @GetMapping("/market/page")
    public Result<PageVO<GoodsVO>> pageMarket(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "12") int size,
                                              @RequestParam(required = false) Integer category,
                                              @RequestParam(required = false) String keyword) {
        return Result.success(goodsService.pageMarket(page, size, category, keyword));
    }

    @GetMapping("/market/{id}")
    public Result<GoodsVO> marketDetail(@PathVariable Long id) {
        return Result.success(goodsService.getMarketDetail(id, UserContext.get().getId()));
    }

    @GetMapping("/{id}/contact")
    public Result<String> sellerContact(@PathVariable Long id) {
        return Result.success(goodsService.getSellerPhone(id, UserContext.get().getId()));
    }
}
