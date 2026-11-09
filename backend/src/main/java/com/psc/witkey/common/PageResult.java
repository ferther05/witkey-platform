package com.psc.witkey.common;

import java.util.List;

/**
 * 分页结果。page 从 1 开始。
 */
public record PageResult<T>(List<T> list, long total, int page, int size) {
}
