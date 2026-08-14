package com.media.website.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResultVO<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResultVO<T> of(List<T> list, long total, int page, int size) {
        PageResultVO<T> vo = new PageResultVO<>();
        vo.setList(list);
        vo.setTotal(total);
        vo.setPage(page);
        vo.setSize(size);
        return vo;
    }
}
