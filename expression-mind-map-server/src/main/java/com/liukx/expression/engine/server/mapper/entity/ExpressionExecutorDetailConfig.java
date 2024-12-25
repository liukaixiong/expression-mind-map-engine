package com.liukx.expression.engine.server.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 表达式配置
 * </p>
 *
 * @author bsy
 * @since 2022-06-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expression_executor_info_config")
public class ExpressionExecutorDetailConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 表达式类型:condition 条件表达式;rule 规则表达式
     */
    private String expressionType;
    /**
     * 执行器id
     */
    private Long executorId;

    /**
     * 上级编号
     */
    private Long parentId;
    /**
     * 表达式编码
     */
    private String expressionCode;
    /**
     * 表达式标题
     */
    private String expressionTitle;
    /**
     * 表达式内容
     */
    private String expressionContent;
    /**
     * 表达式描述
     */
    private String expressionDescription;
    /**
     * 表达式状态:0.禁用,1.启用
     */
    private Boolean expressionStatus;

    /**
     * 优先级顺序,数值越高优先级越高
     */
    private Integer priorityOrder;

    /**
     * 是否已删除:0.否,1.是
     */
    private Boolean deleted;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
