package com.community.service;

import com.community.common.PageVO;
import com.community.dto.RepairCommentDTO;
import com.community.dto.RepairCreateDTO;
import com.community.dto.RepairOrderVO;
import com.community.entity.RepairOrder;

public interface RepairService {

    Long create(RepairCreateDTO dto, Long userId);

    PageVO<RepairOrder> pageForOwner(Long userId, int page, int size, Integer status);

    PageVO<RepairOrderVO> pageForAdmin(int page, int size, Integer status, Integer type, Integer urgency);

    RepairOrderVO getDetail(Long id, Long currentUserId, boolean admin);

    void accept(Long id);

    void start(Long id);

    void complete(Long id, String result);

    void evaluate(Long id, RepairCommentDTO dto, Long userId);

    void cancel(Long id, Long userId);
}
