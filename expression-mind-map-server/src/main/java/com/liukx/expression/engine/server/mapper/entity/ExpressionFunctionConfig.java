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
 * 函数配置表
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expression_function_config")
public class ExpressionFunctionConfig implements Serializable {

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
     * 函数名称
     */
    private String funcName;
    /**
     * 函数描述
     */
    private String funcDescription;
    /**
     * 入参说明
     */
    private String paramDoc;
    /**
     * 是否已删除:0否,1.是
     */
    private Integer deleted;
    /**
     * 状态:0启用，1禁用
     */
    private Integer status;
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

    public ExpressionFunctionConfig() {
    }

}
