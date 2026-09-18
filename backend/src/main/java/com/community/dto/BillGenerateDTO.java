package com.community.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillGenerateDTO {

    private String building;

    @NotNull(message = "请选择费用类型")
    private Integer feeType;

    @NotNull(message = "请输入金额")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    private BigDecimal amount;

    @NotBlank(message = "请选择所属月份")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "所属月份格式应为 yyyy-MM")
    private String month;
}
