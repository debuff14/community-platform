package com.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class Goods {

    @TableId(type = IdType.AUTO)
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
}
