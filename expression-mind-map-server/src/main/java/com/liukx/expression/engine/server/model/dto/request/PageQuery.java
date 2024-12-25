package com.liukx.expression.engine.server.model.dto.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询实体类
 *
 * @author Lion Li
 */

@Data
public class PageQuery implements Serializable {

    /**
     * 当前记录起始索引 默认值
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    /**
     * 每页显示记录数 默认值 默认查全部
     */
    public static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;
    /**
     * 分页大小
     */
    private Integer pageSize;
    /**
     * 当前页数
     */
    private Integer pageNum;
    /**
     * 排序列
     */
    private String orderByColumn;
    /**
     * 排序的方向desc或者asc
     */
    private String isAsc;

}
