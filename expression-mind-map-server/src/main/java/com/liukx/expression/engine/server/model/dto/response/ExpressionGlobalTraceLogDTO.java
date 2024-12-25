package com.liukx.expression.engine.server.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@ApiModel("全局日志记录响应类")
@Data
public class ExpressionGlobalTraceLogDTO implements Serializable {
    /**
     * 主键id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("主键id")
    private Long id;

    /**
     * 服务名称
     */
    @ApiModelProperty("主键id")
    private String serviceName;

    /**
     * 阶段类型
     */
    @ApiModelProperty("阶段类型")
    private String stageType;

    /**
     * 执行链路编号
     */
    @ApiModelProperty("执行链路编号")
    private String linkNo;

    /**
     * 业务编码
     */
    @ApiModelProperty("业务编码")
    private String businessCode;

    /**
     * 事件编码
     */
    @ApiModelProperty("事件编码")
    private String eventCode;

    /**
     * 唯一编号(负责确定唯一编号,类似userId)
     */
    @ApiModelProperty("唯一编号")
    private String uniqueNo;

    /**
     * 执行结果描述
     */
    @ApiModelProperty("执行结果描述")
    private String resultDescription;

    /**
     * 是否执行成功:0.否,1.是
     */
    @ApiModelProperty("是否执行成功")
    private Boolean executeSuccess;


    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    private String updateBy;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;
}
