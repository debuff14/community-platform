package com.community.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsVO {

    private Long id;

    private Long sellerId;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer category;

    private String image;

    private Integer status;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String username;

    private String building;

    private String roomNo;
}
