package com.community.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {

    private List<T> records;
    private long total;
    private long current;
    private long size;

    public static <T> PageVO<T> of(IPage<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.setRecords(page.getRecords());
        vo.setTotal(page.getTotal());
        vo.setCurrent(page.getCurrent());
        vo.setSize(page.getSize());
        return vo;
    }

    public static <T> PageVO<T> of(List<T> records, long total, long current, long size) {
        PageVO<T> vo = new PageVO<>();
        vo.setRecords(records);
        vo.setTotal(total);
        vo.setCurrent(current);
        vo.setSize(size);
        return vo;
    }
}
