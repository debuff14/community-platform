package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.PageVO;
import com.community.common.exception.BusinessException;
import com.community.dto.RepairCommentDTO;
import com.community.dto.RepairCreateDTO;
import com.community.dto.RepairOrderVO;
import com.community.entity.RepairComment;
import com.community.entity.RepairOrder;
import com.community.mapper.RepairCommentMapper;
import com.community.mapper.RepairOrderMapper;
import com.community.service.MessageService;
import com.community.service.RepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairServiceImpl implements RepairService {

    public static final int STATUS_SUBMITTED = 0;
    public static final int STATUS_ACCEPTED = 1;
    public static final int STATUS_PROCESSING = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_EVALUATED = 4;
    public static final int STATUS_CANCELLED = 5;

    private static final int MESSAGE_TYPE_REPAIR = 1;
    private static final int MAX_PAGE_SIZE = 50;

    private final RepairOrderMapper repairOrderMapper;
    private final RepairCommentMapper repairCommentMapper;
    private final MessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RepairCreateDTO dto, Long userId) {
        RepairOrder order = new RepairOrder();
        order.setUserId(userId);
        order.setType(dto.getType());
        order.setDescription(dto.getDescription());
        order.setUrgency(dto.getUrgency());
        order.setImages(dto.getImages() == null || dto.getImages().isEmpty()
                ? null : String.join(",", dto.getImages()));
        order.setStatus(STATUS_SUBMITTED);
        repairOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    public PageVO<RepairOrder> pageForOwner(Long userId, int page, int size, Integer status) {
        int[] normalized = normalize(page, size);
        Page<RepairOrder> dbPage = repairOrderMapper.selectPage(new Page<>(normalized[0], normalized[1]),
                new LambdaQueryWrapper<RepairOrder>()
                        .eq(RepairOrder::getUserId, userId)
                        .eq(status != null, RepairOrder::getStatus, status)
                        .orderByDesc(RepairOrder::getCreateTime)
                        .orderByDesc(RepairOrder::getId));
        return PageVO.of(dbPage);
    }

    @Override
    public PageVO<RepairOrderVO> pageForAdmin(int page, int size, Integer status, Integer type, Integer urgency) {
        int[] normalized = normalize(page, size);
        Page<RepairOrderVO> dbPage = new Page<>(normalized[0], normalized[1]);
        return PageVO.of(repairOrderMapper.selectAdminPage(dbPage, status, type, urgency));
    }

    @Override
    public RepairOrderVO getDetail(Long id, Long currentUserId, boolean admin) {
        RepairOrderVO vo = repairOrderMapper.selectDetailById(id);
        if (vo == null) {
            throw new BusinessException("工单不存在");
        }
        if (!admin && !vo.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权查看他人的工单");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(Long id) {
        RepairOrder order = requireOrder(id);
        if (order.getStatus() != STATUS_SUBMITTED) {
            throw new BusinessException("工单已受理，无法重复接单");
        }
        order.setStatus(STATUS_ACCEPTED);
        order.setAcceptTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
        messageService.push(order.getUserId(), "您的报修工单已被物业接单", MESSAGE_TYPE_REPAIR, order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void start(Long id) {
        RepairOrder order = requireOrder(id);
        if (order.getStatus() != STATUS_ACCEPTED) {
            throw new BusinessException("请先接单后再开始处理");
        }
        order.setStatus(STATUS_PROCESSING);
        repairOrderMapper.updateById(order);
        messageService.push(order.getUserId(), "您的报修工单已开始处理", MESSAGE_TYPE_REPAIR, order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, String result) {
        if (!StringUtils.hasText(result)) {
            throw new BusinessException("请填写处理结果说明");
        }
        RepairOrder order = requireOrder(id);
        if (order.getStatus() != STATUS_PROCESSING) {
            throw new BusinessException("请先接单并开始处理");
        }
        order.setStatus(STATUS_FINISHED);
        order.setResult(result);
        order.setFinishTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
        messageService.push(order.getUserId(), "您的报修工单已处理完成，请及时评价", MESSAGE_TYPE_REPAIR, order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(Long id, RepairCommentDTO dto, Long userId) {
        RepairOrder order = requireOrder(id);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权评价他人的工单");
        }
        if (order.getStatus() == STATUS_EVALUATED) {
            throw new BusinessException("该工单已评价，不能重复评价");
        }
        if (order.getStatus() != STATUS_FINISHED) {
            throw new BusinessException("只有已完成的工单才能评价");
        }
        RepairComment comment = new RepairComment();
        comment.setOrderId(id);
        comment.setUserId(userId);
        comment.setStar(dto.getStar());
        comment.setContent(dto.getContent());
        try {
            repairCommentMapper.insert(comment);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该工单已评价，不能重复评价");
        }
        order.setStatus(STATUS_EVALUATED);
        repairOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, Long userId) {
        RepairOrder order = requireOrder(id);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权取消他人的工单");
        }
        if (order.getStatus() == STATUS_CANCELLED) {
            throw new BusinessException("工单已取消，无需重复操作");
        }
        if (order.getStatus() != STATUS_SUBMITTED) {
            throw new BusinessException("工单已受理，无法取消");
        }
        order.setStatus(STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
    }

    private RepairOrder requireOrder(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        return order;
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
