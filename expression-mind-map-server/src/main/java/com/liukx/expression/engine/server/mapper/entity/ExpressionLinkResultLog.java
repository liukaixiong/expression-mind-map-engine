package com.liukx.expression.engine.server.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 成功回调日志表,记录执行完成的日志记录,全局日志表的压缩版
 * </p>
 *
 * @author bsy
 * @since 2022-06-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@TableName("expression_success_result_log")
@Builder
public class ExpressionLinkResultLog implements Serializable {

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
     * 阶段类型
     */
    private String stageType;

    /**
     * 执行链路编号
     */
    private String linkNo;

    /**
     * 业务编码
     */
    private String businessCode;

    /**
     * 事件编码
     */
    private String eventCode;

    /**
     * 唯一编号(负责确定唯一编号,类似userId)
     */
    private String uniqueNo;

    /**
     * 执行结果描述
     */
    private String resultDescription;

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
