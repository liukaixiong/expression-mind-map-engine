package com.liukx.expression.engine.server.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 表达式历史版本
 * </p>
 *
 * @author expression-engine
 * @since 2025-03-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expression_history_version")
public class ExpressionHistoryVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 表达式id
     */
    private Long expressionId;

    /**
     * 执行器id
     */
    private Long executorId;

    /**
     * 表达式编码
     */
    private String expressionCode;

    /**
     * 表达式标题（历史快照）
     */
    private String expressionTitle;

    /**
     * 表达式内容（历史快照）
     */
    private String expressionContent;

    /**
     * 表达式描述（历史快照）
     */
    private String expressionDescription;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 变更类型: CREATE-创建, UPDATE-更新, DELETE-删除
     */
    private String changeType;

    /**
     * 变更原因（可选）
     */
    private String changeReason;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}