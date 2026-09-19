package com.community.service;

import com.community.common.PageVO;
import com.community.dto.GoodsSaveDTO;
import com.community.dto.GoodsVO;
import com.community.entity.Goods;

public interface GoodsService {

    Long publish(GoodsSaveDTO dto, Long sellerId);

    PageVO<Goods> pageMyGoods(Long sellerId, int page, int size, Integer status);

    void update(Long id, GoodsSaveDTO dto, Long sellerId);

    void offShelf(Long id, Long sellerId);

    PageVO<GoodsVO> pageMarket(int page, int size, Integer category, String keyword);

    GoodsVO getMarketDetail(Long id, Long currentUserId);

    String getSellerPhone(Long id, Long currentUserId);

    PageVO<GoodsVO> pageAdmin(int page, int size, Integer status, String keyword);

    void approve(Long id);

    void reject(Long id, String reason);

    void forceOffShelf(Long id);
}
