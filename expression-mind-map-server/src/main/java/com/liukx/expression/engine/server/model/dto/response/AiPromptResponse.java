package com.liukx.expression.engine.server.model.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("AI提示词响应")
public class AiPromptResponse implements Serializable {

    @ApiModelProperty("Redis Hash field，用于加载和删除")
    private String promptKey;

    @ApiModelProperty("自动生成的文件名标识，如 ai-system-prompt.md、ai-system-prompt-order.md")
    private String fileName;

    @ApiModelProperty("提示词类型")
    private String promptType;

    @ApiModelProperty("服务名称，all 代表所有服务")
    private String serviceName;

    @ApiModelProperty("提示词内容")
    private String content;

    @ApiModelProperty("内容长度")
    private int length;
}
