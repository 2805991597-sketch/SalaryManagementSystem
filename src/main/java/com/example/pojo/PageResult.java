package com.example.pojo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> data;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    public PageResult(List<T> data, Long total, Integer pageNum, Integer pageSize) {
        this.data = data;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(List<T> data, Long total, Integer pageNum, Integer pageSize) {
        return new PageResult<>(data, total, pageNum, pageSize);
    }
}