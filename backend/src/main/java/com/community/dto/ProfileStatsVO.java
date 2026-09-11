package com.community.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfileStatsVO {

    private BigDecimal unpaidAmount;

    private Long unhandledRepairCount;
}
