package com.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.dto.GoodsVO;
import com.community.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    IPage<GoodsVO> selectMarketPage(IPage<GoodsVO> page,
                                    @Param("category") Integer category,
                                    @Param("keyword") String keyword);

    IPage<GoodsVO> selectAdminPage(IPage<GoodsVO> page,
                                   @Param("status") Integer status,
                                   @Param("keyword") String keyword);

    GoodsVO selectGoodsDetail(@Param("id") Long id);
}
