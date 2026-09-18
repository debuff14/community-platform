package com.community.controller;

import com.community.common.PageVO;
import com.community.common.Result;
import com.community.entity.Bill;
import com.community.interceptor.UserContext;
import com.community.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping("/page")
    public Result<PageVO<Bill>> page(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) Integer status) {
        return Result.success(billService.pageForOwner(UserContext.get().getId(), page, size, status));
    }

    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        billService.pay(id, UserContext.get().getId());
        return Result.success("支付成功", null);
    }
}
