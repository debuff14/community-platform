package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.PageVO;
import com.community.common.exception.BusinessException;
import com.community.dto.GoodsSaveDTO;
import com.community.dto.GoodsVO;
import com.community.entity.Goods;
import com.community.entity.User;
import com.community.mapper.GoodsMapper;
import com.community.mapper.UserMapper;
import com.community.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ON_SALE = 1;
    public static final int STATUS_OFF_SHELF = 2;
    public static final int STATUS_REJECTED = 3;

    private static final int MAX_PAGE_SIZE = 50;

    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;

    @Override
    public Long publish(GoodsSaveDTO dto, Long sellerId) {
        Goods goods = new Goods();
        goods.setSellerId(sellerId);
        applyDto(goods, dto);
        goods.setStatus(STATUS_PENDING);
        goodsMapper.insert(goods);
        return goods.getId();
    }

    @Override
    public PageVO<Goods> pageMyGoods(Long sellerId, int page, int size, Integer status) {
        int[] normalized = normalize(page, size);
        Page<Goods> dbPage = goodsMapper.selectPage(new Page<>(normalized[0], normalized[1]),
                new LambdaQueryWrapper<Goods>()
                        .eq(Goods::getSellerId, sellerId)
                        .eq(status != null, Goods::getStatus, status)
                        .orderByDesc(Goods::getCreateTime)
                        .orderByDesc(Goods::getId));
        return PageVO.of(dbPage);
    }

    @Override
    public void update(Long id, GoodsSaveDTO dto, Long sellerId) {
        Goods goods = requireGoods(id);
        if (!goods.getSellerId().equals(sellerId)) {
            throw new BusinessException(403, "无权编辑他人的商品");
        }
        if (goods.getStatus() == STATUS_ON_SALE) {
            throw new BusinessException("在售商品请先下架后再编辑");
        }
        if (goods.getStatus() == STATUS_OFF_SHELF) {
            throw new BusinessException("已下架的商品不能编辑，请重新发布");
        }
        applyDto(goods, dto);
        goods.setStatus(STATUS_PENDING);
        goods.setRejectReason(null);
        goodsMapper.updateById(goods);
    }

    @Override
    public void offShelf(Long id, Long sellerId) {
        Goods goods = requireGoods(id);
        if (!goods.getSellerId().equals(sellerId)) {
            throw new BusinessException(403, "无权下架他人的商品");
        }
        if (goods.getStatus() != STATUS_ON_SALE) {
            throw new BusinessException("只有在售商品可以下架");
        }
        goods.setStatus(STATUS_OFF_SHELF);
        goodsMapper.updateById(goods);
    }

    @Override
    public PageVO<GoodsVO> pageMarket(int page, int size, Integer category, String keyword) {
        int[] normalized = normalize(page, size);
        Page<GoodsVO> dbPage = new Page<>(normalized[0], normalized[1]);
        return PageVO.of(goodsMapper.selectMarketPage(dbPage, category, keyword));
    }

    @Override
    public GoodsVO getMarketDetail(Long id, Long currentUserId) {
        GoodsVO vo = goodsMapper.selectGoodsDetail(id);
        if (vo == null) {
            throw new BusinessException("商品不存在");
        }
        if (vo.getStatus() != STATUS_ON_SALE && !vo.getSellerId().equals(currentUserId)) {
            throw new BusinessException(403, "该商品不可见");
        }
        return vo;
    }

    @Override
    public String getSellerPhone(Long id, Long currentUserId) {
        GoodsVO vo = getMarketDetail(id, currentUserId);
        User seller = userMapper.selectById(vo.getSellerId());
        return seller == null ? null : seller.getPhone();
    }

    @Override
    public PageVO<GoodsVO> pageAdmin(int page, int size, Integer status, String keyword) {
        int[] normalized = normalize(page, size);
        Page<GoodsVO> dbPage = new Page<>(normalized[0], normalized[1]);
        return PageVO.of(goodsMapper.selectAdminPage(dbPage, status, keyword));
    }

    @Override
    public void approve(Long id) {
        Goods goods = requireGoods(id);
        if (goods.getStatus() != STATUS_PENDING) {
            throw new BusinessException("只有待审核商品可以审核通过");
        }
        goods.setStatus(STATUS_ON_SALE);
        goods.setRejectReason(null);
        goodsMapper.updateById(goods);
    }

    @Override
    public void reject(Long id, String reason) {
        Goods goods = requireGoods(id);
        if (goods.getStatus() != STATUS_PENDING) {
            throw new BusinessException("只有待审核商品可以驳回");
        }
        goods.setStatus(STATUS_REJECTED);
        goods.setRejectReason(reason);
        goodsMapper.updateById(goods);
    }

    @Override
    public void forceOffShelf(Long id) {
        Goods goods = requireGoods(id);
        if (goods.getStatus() != STATUS_ON_SALE) {
            throw new BusinessException("只有在售商品可以强制下架");
        }
        goods.setStatus(STATUS_OFF_SHELF);
        goodsMapper.updateById(goods);
    }

    private void applyDto(Goods goods, GoodsSaveDTO dto) {
        goods.setName(dto.getName());
        goods.setDescription(dto.getDescription());
        goods.setPrice(dto.getPrice());
        goods.setCategory(dto.getCategory());
        goods.setImage(dto.getImage());
    }

    private Goods requireGoods(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        return goods;
    }

    private int[] normalize(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            size = 10;
        }
        return new int[]{page, size};
    }
}
