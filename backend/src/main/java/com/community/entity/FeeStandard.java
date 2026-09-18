package com.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fee_standard")
public class FeeStandard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer feeType;

    private BigDecimal amount;

    private LocalDateTime updateTime;
}
