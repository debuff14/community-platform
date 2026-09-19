package com.community.service;

import com.community.common.PageVO;
import com.community.dto.VisitorCreateDTO;
import com.community.dto.VisitorVO;
import com.community.entity.Visitor;

public interface VisitorService {

    Long register(VisitorCreateDTO dto, Long userId);

    PageVO<Visitor> pageMyVisitors(Long userId, int page, int size);

    void cancel(Long id, Long userId);

    PageVO<VisitorVO> pageAdmin(int page, int size);

    void enter(Long id);

    void leave(Long id);
}
