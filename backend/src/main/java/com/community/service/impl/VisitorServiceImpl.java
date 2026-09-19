package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.PageVO;
import com.community.common.exception.BusinessException;
import com.community.dto.VisitorCreateDTO;
import com.community.dto.VisitorVO;
import com.community.entity.Visitor;
import com.community.mapper.VisitorMapper;
import com.community.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {

    public static final int STATUS_WAITING = 0;
    public static final int STATUS_ENTERED = 1;
    public static final int STATUS_LEFT = 2;
    public static final int STATUS_CANCELLED = 3;

    private static final int MAX_PAGE_SIZE = 50;

    private final VisitorMapper visitorMapper;

    @Override
    public Long register(VisitorCreateDTO dto, Long userId) {
        if (!dto.getTimeEnd().isAfter(dto.getTimeStart())) {
            throw new BusinessException("预计结束时间必须晚于开始时间");
        }
        Visitor visitor = new Visitor();
        visitor.setUserId(userId);
        visitor.setVisitorName(dto.getVisitorName());
        visitor.setCarNo(dto.getCarNo());
        visitor.setVisitDate(dto.getVisitDate());
        visitor.setTimeStart(dto.getTimeStart());
        visitor.setTimeEnd(dto.getTimeEnd());
        visitor.setStatus(STATUS_WAITING);
        visitorMapper.insert(visitor);
        return visitor.getId();
    }

    @Override
    public PageVO<Visitor> pageMyVisitors(Long userId, int page, int size) {
        int[] normalized = normalize(page, size);
        Page<Visitor> dbPage = visitorMapper.selectPage(new Page<>(normalized[0], normalized[1]),
                new LambdaQueryWrapper<Visitor>()
                        .eq(Visitor::getUserId, userId)
                        .orderByDesc(Visitor::getVisitDate)
                        .orderByDesc(Visitor::getId));
        LocalDate today = LocalDate.now();
        for (Visitor visitor : dbPage.getRecords()) {
            visitor.setExpired(visitor.getStatus() == STATUS_WAITING
                    && visitor.getVisitDate() != null && visitor.getVisitDate().isBefore(today));
        }
        return PageVO.of(dbPage);
    }

    @Override
    public void cancel(Long id, Long userId) {
        Visitor visitor = requireVisitor(id);
        if (!visitor.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权取消他人的访客登记");
        }
        if (visitor.getStatus() != STATUS_WAITING) {
            throw new BusinessException("只有待到访的登记可以取消");
        }
        visitor.setStatus(STATUS_CANCELLED);
        visitorMapper.updateById(visitor);
    }

    @Override
    public PageVO<VisitorVO> pageAdmin(int page, int size) {
        int[] normalized = normalize(page, size);
        Page<VisitorVO> dbPage = new Page<>(normalized[0], normalized[1]);
        PageVO<VisitorVO> vo = PageVO.of(visitorMapper.selectAdminPage(dbPage, LocalDate.now()));
        LocalDate today = LocalDate.now();
        for (VisitorVO visitor : vo.getRecords()) {
            visitor.setExpired(visitor.getStatus() == STATUS_WAITING
                    && visitor.getVisitDate() != null && visitor.getVisitDate().isBefore(today));
        }
        return vo;
    }

    @Override
    public void enter(Long id) {
        Visitor visitor = requireVisitor(id);
        if (visitor.getStatus() != STATUS_WAITING) {
            throw new BusinessException("该访客当前不可登记入场");
        }
        visitor.setStatus(STATUS_ENTERED);
        visitor.setEnterTime(LocalDateTime.now());
        visitorMapper.updateById(visitor);
    }

    @Override
    public void leave(Long id) {
        Visitor visitor = requireVisitor(id);
        if (visitor.getStatus() != STATUS_ENTERED) {
            throw new BusinessException("只有已入场的访客可以登记离场");
        }
        visitor.setStatus(STATUS_LEFT);
        visitor.setLeaveTime(LocalDateTime.now());
        visitorMapper.updateById(visitor);
    }

    private Visitor requireVisitor(Long id) {
        Visitor visitor = visitorMapper.selectById(id);
        if (visitor == null) {
            throw new BusinessException("访客登记不存在");
        }
        return visitor;
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
