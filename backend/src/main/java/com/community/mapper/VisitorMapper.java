package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.dto.VisitorVO;
import com.community.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {

    IPage<VisitorVO> selectAdminPage(IPage<VisitorVO> page, @Param("fromDate") LocalDate fromDate);
}
