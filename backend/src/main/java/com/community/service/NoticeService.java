package com.community.service;

import com.community.common.PageVO;
import com.community.dto.NoticeDTO;
import com.community.entity.Notice;

public interface NoticeService {

    PageVO<Notice> pageForOwner(int page, int size);

    PageVO<Notice> pageForAdmin(int page, int size, String keyword);

    Notice getDetail(Long id);

    void add(NoticeDTO dto, Long adminId);

    void update(Long id, NoticeDTO dto);

    void delete(Long id);
}
