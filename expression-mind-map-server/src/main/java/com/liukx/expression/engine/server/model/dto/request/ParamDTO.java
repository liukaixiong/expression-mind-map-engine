package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("参数设置类")
public class ParamDTO implements Serializable {
    @ApiModelProperty(value = "参数名称", required = true)
    private String paramName;

    @ApiModelProperty("参数描述")
    private String paramDescription;

    @ApiModelProperty(value = "参数排序", required = true)
    private Integer paramSortNum;

    @ApiModelProperty(value = "参数类型", required = true, allowableValues = "String,Integer,Long,List", example = "String")
    private String paramDataType;
}
