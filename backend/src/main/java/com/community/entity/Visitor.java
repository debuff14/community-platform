package com.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("visitor")
public class Visitor {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String visitorName;

    private String carNo;

    private LocalDate visitDate;

    private LocalTime timeStart;

    private LocalTime timeEnd;

    private Integer status;

    private LocalDateTime enterTime;

    private LocalDateTime leaveTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Boolean expired;
}
