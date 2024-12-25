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
 *
 * </p>
 *
 * @author bsy
 * @since 2022-06-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expression_executor_base_info")
public class ExpressionExecutorBaseInfo implements Serializable {

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
     * 业务编码
     */
    private String businessCode;
    /**
     * 执行器编码
     */
    private String executorCode;
    /**
     * 执行器描述
     */
    private String executorDescription;

    /**
     * 配置能力开关
     */
    private String configurabilityJson;
    /**
     * 变量定义,方便索引
     */
    private String varDefinition;
    /**
     * 是否已删除:0否,1.是
     */
    private Boolean deleted;

    /**
     * 执行器状态:0.创建，1.启用，2.禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

}
