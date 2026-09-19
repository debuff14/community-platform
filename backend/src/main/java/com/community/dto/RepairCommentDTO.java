package com.community.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepairCommentDTO {

    @NotNull(message = "请选择星级")
    @Min(value = 1, message = "星级最低为1星")
    @Max(value = 5, message = "星级最高为5星")
    private Integer star;

    @Size(max = 500, message = "评语不能超过500字")
    private String content;
}
