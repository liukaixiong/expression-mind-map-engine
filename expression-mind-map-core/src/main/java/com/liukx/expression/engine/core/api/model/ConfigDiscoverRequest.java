package com.liukx.expression.engine.core.api.model;

import lombok.Data;

/**
 * 配置发现请求模型
 *
 * @author liukaixiong
 * @date 2023/12/11
 */
@Data
public class ConfigDiscoverRequest {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 配置编码
     */
    private String businessCode;

    /**
     * 执行器编码
     */
    private String executorCode;

    /**
     * 时间戳版本
     */
    private Long timestamp;

}
