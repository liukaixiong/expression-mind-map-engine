package com.liukx.expression.engine.server.model.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("编辑规则引擎函数请求类")
@Data
public class EditExpressionFunctionRequest implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "函数id", required = true)
    private Long id;
    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serviceName;

    /**
     * 函数描述
     */
    @ApiModelProperty("函数描述")
    private String funcDescription;

    /**
     * 函数入参json说明
     */
    @ApiModelProperty("函数入参json说明")
    private String bodyParam;
    /**
     * 函数描述
     */
    @ApiModelProperty("函数描述")
    private String paramDoc;
    /**
     * 公共入参
     */
    @ApiModelProperty("公共入参")
    private String commonParam;

    /**
     * 状态:0启用，1禁用
     */
    @ApiModelProperty("函数状态")
    private Integer status;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    private String updateBy;

}
