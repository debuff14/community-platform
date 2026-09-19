package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GoodsRejectDTO {

    @NotBlank(message = "请填写驳回原因")
    @Size(max = 255, message = "驳回原因不能超过255字")
    private String reason;
}
