package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("AI提示词请求")
public class AiPromptRequest implements Serializable {

    @ApiModelProperty("提示词类型：SYSTEM")
    private String promptType;

    @ApiModelProperty("服务名称，默认 all 代表所有服务")
    private String serviceName = "all";

    @ApiModelProperty("提示词内容")
    private String content;
}
