package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM bill WHERE user_id = #{userId} AND status = 0 AND month = #{month}")
    BigDecimal sumUnpaidAmount(@Param("userId") Long userId, @Param("month") String month);
}
