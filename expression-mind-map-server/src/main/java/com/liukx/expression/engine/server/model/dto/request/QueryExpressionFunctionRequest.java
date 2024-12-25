package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("查询函数请求类")
public class QueryExpressionFunctionRequest implements Serializable {
    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serviceName;

    /**
     * 函数名称
     */
    @ApiModelProperty("函数名称")
    private String funcName;

}
