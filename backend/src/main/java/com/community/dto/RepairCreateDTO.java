package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RepairCreateDTO {

    @NotNull(message = "请选择报修类型")
    private Integer type;

    @NotBlank(message = "请填写问题描述")
    @Size(max = 500, message = "问题描述不能超过500字")
    private String description;

    @NotNull(message = "请选择紧急程度")
    private Integer urgency;

    @Size(max = 3, message = "最多上传3张现场照片")
    private List<String> images;
}
