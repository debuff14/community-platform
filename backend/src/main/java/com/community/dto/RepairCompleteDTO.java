package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepairCompleteDTO {

    @NotBlank(message = "请填写处理结果说明")
    @Size(max = 500, message = "处理结果说明不能超过500字")
    private String result;
}
