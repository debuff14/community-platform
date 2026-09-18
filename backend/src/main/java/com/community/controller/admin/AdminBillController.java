package com.community.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.community.common.PageVO;
import com.community.common.Result;
import com.community.dto.BillExcelVO;
import com.community.dto.BillGenerateDTO;
import com.community.dto.BillGenerateVO;
import com.community.dto.BillVO;
import com.community.dto.FeeStandardDTO;
import com.community.entity.FeeStandard;
import com.community.service.BillService;
import com.community.service.FeeStandardService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/admin/bill")
@RequiredArgsConstructor
public class AdminBillController {

    private final BillService billService;
    private final FeeStandardService feeStandardService;

    @GetMapping("/page")
    public Result<PageVO<BillVO>> page(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String building,
                                       @RequestParam(required = false) String month) {
        return Result.success(billService.pageForAdmin(page, size, status, building, month));
    }

    @PostMapping("/generate")
    public Result<BillGenerateVO> generate(@Valid @RequestBody BillGenerateDTO dto) {
        return Result.success("账单生成完成", billService.generate(dto, null));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        billService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String building,
                       @RequestParam(required = false) String month,
                       HttpServletResponse response) throws IOException {
        List<BillExcelVO> list = billService.listForExport(status, building, month);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String fileName = URLEncoder.encode("账单明细", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName + ".xlsx");
        response.setHeader("X-Total-Count", String.valueOf(list.size()));
        EasyExcel.write(response.getOutputStream(), BillExcelVO.class).sheet("账单明细").doWrite(list);
    }

    @GetMapping("/standard")
    public Result<List<FeeStandard>> standard() {
        return Result.success(feeStandardService.list());
    }

    @PutMapping("/standard")
    public Result<Void> updateStandard(@RequestBody List<FeeStandardDTO> standards) {
        feeStandardService.updateAll(standards);
        return Result.success("保存成功", null);
    }
}
