package com.liukx.expression.engine.core.api.model.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liukaixiong
 * @date 2024/7/18 - 15:19
 */
@Data
public class ExpressionExecutorResultDTO {

    /**
     * 执行器编号
     */
    private Long executorId;
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 确定组名称,比如活动code
     */
    private String businessCode;

    /**
     * 代表事件名称,确定业务事件
     */
    private String eventName;

    /**
     * 执行器编码
     */
    private String executorCode;

    /**
     * 执行器名称
     */
    private String executorName;

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 唯一编号
     */
    private String unionId;
    /**
     * 链路编号
     */
    private String traceId;

    /**
     * 上下文参数
     */
    private String envBody;

    /**
     * 表达式执行列表
     */
    private List<ExpressionResultLogDTO> resultLogList = new ArrayList<>();
}
