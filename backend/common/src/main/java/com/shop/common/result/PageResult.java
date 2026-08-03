package com.shop.common.result;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private long total;
    private long current;
    private long size;
    private long pages;
    private List<T> records;

    public static <T> PageResult<T> of(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.total = page.getTotal();
        result.current = page.getCurrent();
        result.size = page.getSize();
        result.pages = page.getPages();
        result.records = page.getRecords();
        return result;
    }

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.records = records;
        result.total = total;
        result.current = current;
        result.size = size;
        result.pages = size == 0 ? 0 : (total + size - 1) / size;
        return result;
    }
}

