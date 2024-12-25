package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel("添加规则引擎函数请求类")
@Data
public class AddExpressionFunctionRequest implements Serializable {
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

    /**
     * 函数描述
     */
    @ApiModelProperty("函数描述")
    private String funcDescription;

    /**
     * 函数入参json说明
     */
    @ApiModelProperty("函数入参json说明")
    private List<ParamDTO> bodyParam;

    /**
     * 公共入参
     */
    @ApiModelProperty("公共入参")
    private List<ParamDTO> commonParam;

    /**
     * 状态:0启用，1禁用
     */
    @ApiModelProperty("函数状态")
    private Integer status;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

}
