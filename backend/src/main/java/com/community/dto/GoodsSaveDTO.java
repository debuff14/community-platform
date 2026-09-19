package com.community.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsSaveDTO {

    @NotBlank(message = "请填写商品名称")
    @Size(max = 100, message = "商品名称不能超过100字")
    private String name;

    @Size(max = 1000, message = "商品描述不能超过1000字")
    private String description;

    @NotNull(message = "请填写价格")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    @Digits(integer = 8, fraction = 2, message = "价格最多保留两位小数")
    private BigDecimal price;

    @NotNull(message = "请选择商品分类")
    private Integer category;

    @NotBlank(message = "请上传商品图片")
    private String image;
}
