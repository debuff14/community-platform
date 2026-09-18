package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.dto.FeeStandardDTO;
import com.community.entity.FeeStandard;
import com.community.mapper.FeeStandardMapper;
import com.community.service.FeeStandardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeStandardServiceImpl implements FeeStandardService {

    private final FeeStandardMapper feeStandardMapper;

    @Override
    public List<FeeStandard> list() {
        return feeStandardMapper.selectList(new LambdaQueryWrapper<FeeStandard>()
                .orderByAsc(FeeStandard::getFeeType));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAll(List<FeeStandardDTO> standards) {
        for (FeeStandardDTO dto : standards) {
            if (dto.getFeeType() == null || dto.getAmount() == null
                    || dto.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new com.community.common.exception.BusinessException("收费标准金额必须大于 0");
            }
            FeeStandard standard = feeStandardMapper.selectOne(new LambdaQueryWrapper<FeeStandard>()
                    .eq(FeeStandard::getFeeType, dto.getFeeType()));
            if (standard == null) {
                standard = new FeeStandard();
                standard.setFeeType(dto.getFeeType());
                standard.setAmount(dto.getAmount());
                feeStandardMapper.insert(standard);
            } else {
                standard.setAmount(dto.getAmount());
                feeStandardMapper.updateById(standard);
            }
        }
    }
}
