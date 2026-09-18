package com.community.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillVO {

    private Long id;

    private Long userId;

    private String username;

    private String building;

    private String roomNo;

    private Integer feeType;

    private BigDecimal amount;

    private String month;

    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime createTime;
}
