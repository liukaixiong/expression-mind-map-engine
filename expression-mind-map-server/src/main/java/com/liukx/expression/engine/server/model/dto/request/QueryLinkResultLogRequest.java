package com.liukx.expression.engine.server.model.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("查询链路成功回调日志请求类")
public class QueryLinkResultLogRequest implements Serializable {
    /**
     * 主键id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("主键id")
    private Long id;
    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
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
}
