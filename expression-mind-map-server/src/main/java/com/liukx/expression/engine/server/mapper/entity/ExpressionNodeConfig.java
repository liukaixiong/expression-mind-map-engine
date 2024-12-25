package com.liukx.expression.engine.server.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 引擎节点信息
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("expression_node_config")
@Builder
public class ExpressionNodeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 调用方式
     */
    private String callMethod;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务描述
     */
    private String serviceDescription;

    /**
     * 项目地址
     */
    private String domain;

    /**
     * 是否已删除:0.否，1.是
     */
    private Boolean deleted;

    /**
     * 状态:0.启用1.禁用
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
