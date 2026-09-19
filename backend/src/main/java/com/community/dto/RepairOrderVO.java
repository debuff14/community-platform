package com.community.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairOrderVO {

    private Long id;

    private Long userId;

    private String username;

    private String building;

    private String roomNo;

    private Integer type;

    private String description;

    private Integer urgency;

    private String images;

    private Integer status;

    private String result;

    private LocalDateTime acceptTime;

    private LocalDateTime finishTime;

    private LocalDateTime cancelTime;

    private LocalDateTime createTime;

    private Integer star;

    private String commentContent;
}
