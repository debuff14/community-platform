package com.community.service;

import com.community.common.exception.BusinessException;
import com.community.dto.RepairCommentDTO;
import com.community.entity.RepairComment;
import com.community.entity.RepairOrder;
import com.community.mapper.RepairCommentMapper;
import com.community.mapper.RepairOrderMapper;
import com.community.service.impl.RepairServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("工单状态流转校验")
class RepairServiceStateTest {

    @Mock
    private RepairOrderMapper repairOrderMapper;

    @Mock
    private RepairCommentMapper repairCommentMapper;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private RepairServiceImpl repairService;

    private RepairOrder order(long id, long userId, int status) {
        RepairOrder order = new RepairOrder();
        order.setId(id);
        order.setUserId(userId);
        order.setStatus(status);
        return order;
    }

    @Test
    @DisplayName("已提交工单可接单，记录接单时间并推送消息")
    void acceptSubmittedOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_SUBMITTED));

        repairService.accept(1L);

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repairOrderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(RepairServiceImpl.STATUS_ACCEPTED);
        assertThat(captor.getValue().getAcceptTime()).isNotNull();
        verify(messageService).push(eq(2L), anyString(), eq(1), eq(1L));
    }

    @Test
    @DisplayName("已接单的工单再次接单被拒绝")
    void rejectAcceptAcceptedOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_ACCEPTED));

        assertThatThrownBy(() -> repairService.accept(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无法重复接单");
        verify(repairOrderMapper, never()).updateById(any(RepairOrder.class));
    }

    @Test
    @DisplayName("未接单的工单不能开始处理")
    void rejectStartBeforeAccept() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_SUBMITTED));

        assertThatThrownBy(() -> repairService.start(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("请先接单");
    }

    @Test
    @DisplayName("未接单直接点击完成被拒绝")
    void rejectCompleteBeforeAccept() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_SUBMITTED));

        assertThatThrownBy(() -> repairService.complete(1L, "已处理"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("请先接单并开始处理");
    }

    @Test
    @DisplayName("处理中的工单可完成，记录处理说明与完成时间")
    void completeProcessingOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_PROCESSING));

        repairService.complete(1L, "已更换水阀，试水正常");

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repairOrderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(RepairServiceImpl.STATUS_FINISHED);
        assertThat(captor.getValue().getResult()).isEqualTo("已更换水阀，试水正常");
        assertThat(captor.getValue().getFinishTime()).isNotNull();
    }

    @Test
    @DisplayName("处理说明为空不能完成工单")
    void rejectCompleteWithBlankResult() {
        assertThatThrownBy(() -> repairService.complete(1L, "   "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("请填写处理结果说明");
        verify(repairOrderMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("已完成的工单可评价，状态变为已评价")
    void evaluateFinishedOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_FINISHED));
        RepairCommentDTO dto = new RepairCommentDTO();
        dto.setStar(5);
        dto.setContent("很快修好了");

        repairService.evaluate(1L, dto, 2L);

        verify(repairCommentMapper).insert(any(RepairComment.class));
        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repairOrderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(RepairServiceImpl.STATUS_EVALUATED);
    }

    @Test
    @DisplayName("已评价的工单不能重复评价")
    void rejectEvaluateEvaluatedOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_EVALUATED));
        RepairCommentDTO dto = new RepairCommentDTO();
        dto.setStar(4);

        assertThatThrownBy(() -> repairService.evaluate(1L, dto, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能重复评价");
    }

    @Test
    @DisplayName("未完成的工单不能评价")
    void rejectEvaluateUnfinishedOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_ACCEPTED));
        RepairCommentDTO dto = new RepairCommentDTO();
        dto.setStar(5);

        assertThatThrownBy(() -> repairService.evaluate(1L, dto, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有已完成的工单才能评价");
    }

    @Test
    @DisplayName("已提交的工单可取消")
    void cancelSubmittedOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_SUBMITTED));

        repairService.cancel(1L, 2L);

        ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
        verify(repairOrderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(RepairServiceImpl.STATUS_CANCELLED);
        assertThat(captor.getValue().getCancelTime()).isNotNull();
    }

    @Test
    @DisplayName("已接单后业主取消被拒绝")
    void rejectCancelAcceptedOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_ACCEPTED));

        assertThatThrownBy(() -> repairService.cancel(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("工单已受理，无法取消");
    }

    @Test
    @DisplayName("不能取消他人工单")
    void rejectCancelOthersOrder() {
        when(repairOrderMapper.selectById(1L)).thenReturn(order(1L, 2L, RepairServiceImpl.STATUS_SUBMITTED));

        assertThatThrownBy(() -> repairService.cancel(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权取消他人的工单");
    }

    @Test
    @DisplayName("完整合法流转：提交→接单→处理中→完成→评价")
    void fullLegalTransition() {
        RepairOrder order = order(1L, 2L, RepairServiceImpl.STATUS_SUBMITTED);
        when(repairOrderMapper.selectById(1L)).thenReturn(order);

        repairService.accept(1L);
        assertThat(order.getStatus()).isEqualTo(RepairServiceImpl.STATUS_ACCEPTED);

        repairService.start(1L);
        assertThat(order.getStatus()).isEqualTo(RepairServiceImpl.STATUS_PROCESSING);

        repairService.complete(1L, "已更换水阀");
        assertThat(order.getStatus()).isEqualTo(RepairServiceImpl.STATUS_FINISHED);

        RepairCommentDTO dto = new RepairCommentDTO();
        dto.setStar(5);
        repairService.evaluate(1L, dto, 2L);
        assertThat(order.getStatus()).isEqualTo(RepairServiceImpl.STATUS_EVALUATED);

        verify(messageService, times(3)).push(eq(2L), anyString(), eq(1), eq(1L));
    }
}
