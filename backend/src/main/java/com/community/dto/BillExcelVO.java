package com.community.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillExcelVO {

    @ExcelProperty("业主姓名")
    private String username;

    @ExcelProperty("楼栋房号")
    private String address;

    @ExcelProperty("费用类型")
    private String feeTypeName;

    @ExcelProperty("金额")
    private BigDecimal amount;

    @ExcelProperty("所属月份")
    private String month;

    @ExcelProperty("状态")
    private String statusName;

    @ExcelProperty("支付时间")
    private String payTime;
}
