package com.liukx.expression.engine.server.executor;

import com.liukx.expression.engine.core.api.model.ApiResult;
import com.liukx.expression.engine.core.api.model.api.RemoteExpressionModel;
import com.liukx.expression.engine.server.enums.RemoteInvokeTypeEnums;
import com.liukx.expression.engine.server.service.model.node.NodeServiceDto;

/**
 * 调用远端执行器定义
 *
 * @author liukaixiong
 * @date : 2022/6/9 - 14:10
 */
public interface ExpressionRemoteInvoker {

    /**
     * 执行类型
     *
     * @return
     */
    public RemoteInvokeTypeEnums type();

    default String parseDomain(NodeServiceDto domain) {
        return domain.getDomain();
    }

    /**
     * 具体的执行方式
     *
     * @param url
     * @param expressionModel
     * @return
     * @throws Exception
     */
    public ApiResult<String> invoke(String url, RemoteExpressionModel expressionModel) throws Exception;
}
