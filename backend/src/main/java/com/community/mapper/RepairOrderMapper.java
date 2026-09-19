package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.dto.RepairOrderVO;
import com.community.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    @Select("SELECT COUNT(*) FROM repair_order WHERE user_id = #{userId} AND status IN (0, 1, 2)")
    long countUnhandled(@Param("userId") Long userId);

    IPage<RepairOrderVO> selectAdminPage(IPage<RepairOrderVO> page,
                                         @Param("status") Integer status,
                                         @Param("type") Integer type,
                                         @Param("urgency") Integer urgency);

    RepairOrderVO selectDetailById(@Param("id") Long id);
}
