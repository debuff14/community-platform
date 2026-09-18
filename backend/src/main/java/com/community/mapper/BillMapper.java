package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.dto.BillVO;
import com.community.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM bill WHERE user_id = #{userId} AND status = 0 AND month = #{month}")
    BigDecimal sumUnpaidAmount(@Param("userId") Long userId, @Param("month") String month);

    IPage<BillVO> selectAdminPage(IPage<BillVO> page,
                                  @Param("status") Integer status,
                                  @Param("building") String building,
                                  @Param("month") String month);

    List<BillVO> selectAdminList(@Param("status") Integer status,
                                 @Param("building") String building,
                                 @Param("month") String month);

    @Update("UPDATE bill SET status = 1, pay_time = #{payTime} WHERE id = #{id} AND status = 0")
    int updateStatusToPaid(@Param("id") Long id, @Param("payTime") LocalDateTime payTime);

    @Select("SELECT COUNT(*) FROM bill WHERE user_id = #{userId} AND status = 0 AND month = #{month}")
    long countUnpaidInMonth(@Param("userId") Long userId, @Param("month") String month);
}
