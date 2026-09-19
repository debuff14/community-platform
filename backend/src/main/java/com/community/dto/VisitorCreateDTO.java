package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VisitorCreateDTO {

    @NotBlank(message = "请填写访客姓名")
    @Size(max = 50, message = "访客姓名不能超过50字")
    private String visitorName;

    @Size(max = 20, message = "车牌号不能超过20位")
    private String carNo;

    @NotNull(message = "请选择来访日期")
    private LocalDate visitDate;

    @NotNull(message = "请选择预计开始时间")
    private LocalTime timeStart;

    @NotNull(message = "请选择预计结束时间")
    private LocalTime timeEnd;
}
