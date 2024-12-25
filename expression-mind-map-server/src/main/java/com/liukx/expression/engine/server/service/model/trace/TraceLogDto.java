package com.liukx.expression.engine.server.service.model.trace;

import lombok.Builder;
import lombok.Data;

/**
 * 链路日志
 * @author liukaixiong
 * @date : 2022/6/20 - 10:55
 */
@Builder
@Data
public class TraceLogDto {

    private String serviceName;
    private String businessCode;
    private String unionId;
    private String logId;
    private String eventType;
    private String content;
    private boolean isException;

}
