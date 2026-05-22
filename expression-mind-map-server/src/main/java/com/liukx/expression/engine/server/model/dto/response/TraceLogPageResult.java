package com.liukx.expression.engine.server.model.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 追踪日志通用分页结果
 * <p>
 * 不依赖任何 ORM 框架，供 {@code TraceLogStorageService} 扩展点接口使用。
 * </p>
 *
 * @param <T> 记录类型
 * @author liukx
 */
@Data
public class TraceLogPageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records = new ArrayList<>();

    /**
     * 总记录数（近似值，用于前端判断是否还有更多数据）
     */
    private long total;

    public TraceLogPageResult() {
    }

    public TraceLogPageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }
}
