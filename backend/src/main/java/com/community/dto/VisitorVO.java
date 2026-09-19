package com.community.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class VisitorVO {

    private Long id;

    private Long userId;

    private String username;

    private String building;

    private String roomNo;

    private String visitorName;

    private String carNo;

    private LocalDate visitDate;

    private LocalTime timeStart;

    private LocalTime timeEnd;

    private Integer status;

    private LocalDateTime enterTime;

    private LocalDateTime leaveTime;

    private LocalDateTime createTime;

    private Boolean expired;
}
