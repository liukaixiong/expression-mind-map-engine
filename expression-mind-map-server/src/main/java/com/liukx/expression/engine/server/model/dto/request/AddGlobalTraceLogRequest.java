package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("添加全局日志请求类")
public class AddGlobalTraceLogRequest implements Serializable {
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

    /**
     * 执行结果描述
     */
    @ApiModelProperty("执行结果描述")
    private String resultDescription;

    /**
     * 是否执行成功:0.否,1.是
     */
    @ApiModelProperty("执行链路编号")
    private Boolean executeSuccess;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;


}
