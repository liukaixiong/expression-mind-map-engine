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
 * 表达式引擎通用变量配置表
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expression_variable_config")
public class ExpressionVariableConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 变量编码
     */
    private String varCode;
    /**
     * 变量描述
     */
    private String varDescription;
    /**
     * 变量来源:local本地,remote远程
     */
    private String varSource;
    /**
     * 变量数据类型
     */
    private String varDataType;
    /**
     * 状态:0.启用,1.禁用
     */
    private Integer status;
    /**
     * 是否已删除:0.否
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

    public ExpressionVariableConfig() {
    }
}
