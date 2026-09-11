package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.PageVO;
import com.community.common.exception.BusinessException;
import com.community.dto.NoticeDTO;
import com.community.entity.Notice;
import com.community.mapper.NoticeMapper;
import com.community.service.NoticeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private static final String CACHE_KEY_PREFIX = "notice:page:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    private static final int MAX_PAGE_SIZE = 50;

    private final NoticeMapper noticeMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public PageVO<Notice> pageForOwner(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            size = 10;
        }
        int finalPage = page;
        int finalSize = size;
        String cacheKey = CACHE_KEY_PREFIX + finalPage + ":" + finalSize;

        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<PageVO<Notice>>() {
                });
            }
        } catch (Exception e) {
            log.warn("读取公告缓存失败，回退数据库查询", e);
        }

        PageVO<Notice> vo = queryFromDb(finalPage, finalSize);

        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(vo), CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入公告缓存失败", e);
        }
        return vo;
    }

    private PageVO<Notice> queryFromDb(int page, int size) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getIsTop, 0)
                .orderByDesc(Notice::getCreateTime)
                .orderByDesc(Notice::getId);
        Page<Notice> dbPage = noticeMapper.selectPage(new Page<>(page, size), wrapper);

        List<Notice> records = new ArrayList<>(dbPage.getRecords());
        if (page == 1) {
            List<Notice> pinnedList = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                    .eq(Notice::getIsTop, 1)
                    .orderByDesc(Notice::getCreateTime)
                    .orderByDesc(Notice::getId));
            records.addAll(0, pinnedList);
        }
        return PageVO.of(records, dbPage.getTotal(), page, size);
    }

    @Override
    public PageVO<Notice> pageForAdmin(int page, int size, String keyword) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            size = 10;
        }
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .like(StringUtils.hasText(keyword), Notice::getTitle, keyword)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getCreateTime)
                .orderByDesc(Notice::getId);
        Page<Notice> dbPage = noticeMapper.selectPage(new Page<>(page, size), wrapper);
        return PageVO.of(dbPage);
    }

    @Override
    public Notice getDetail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在或已被删除");
        }
        return notice;
    }

    @Override
    public void add(NoticeDTO dto, Long adminId) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setIsTop(dto.getIsTop());
        notice.setAdminId(adminId);
        noticeMapper.insert(notice);
        clearCache();
    }

    @Override
    public void update(Long id, NoticeDTO dto) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在或已被删除");
        }
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setIsTop(dto.getIsTop());
        noticeMapper.updateById(notice);
        clearCache();
    }

    @Override
    public void delete(Long id) {
        if (noticeMapper.selectById(id) == null) {
            throw new BusinessException("公告不存在或已被删除");
        }
        noticeMapper.deleteById(id);
        clearCache();
    }

    private void clearCache() {
        Set<String> keys = stringRedisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }
}
