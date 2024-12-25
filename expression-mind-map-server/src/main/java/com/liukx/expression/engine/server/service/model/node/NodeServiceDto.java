package com.liukx.expression.engine.server.service.model.node;

import com.liukx.expression.engine.server.enums.RemoteInvokeTypeEnums;
import lombok.Builder;
import lombok.Data;

/**
 * 节点服务传输数据
 * @author liukaixiong
 * @date : 2022/6/9 - 16:51
 */
@Data
@Builder
public class NodeServiceDto {
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 服务路由地址,精确到服务路径就行
     */
    private String domain;
    /**
     * 请求实现类型
     */
    private RemoteInvokeTypeEnums remoteInvokeType;

}
