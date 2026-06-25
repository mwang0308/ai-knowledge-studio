package com.aistudio.foundation.domain;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回结构。
 *
 * @param total 总记录数
 * @param pageNo 当前页码
 * @param pageSize 每页条数
 * @param records 当前页记录
 * @param <T> 记录类型
 */
public record PageResult<T>(long total, long pageNo, long pageSize, List<T> records) {

    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return new PageResult<>(0, pageNo, pageSize, Collections.emptyList());
    }
}
