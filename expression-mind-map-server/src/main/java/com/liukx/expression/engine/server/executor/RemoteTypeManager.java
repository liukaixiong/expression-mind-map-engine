package com.liukx.expression.engine.server.executor;

import com.liukx.expression.engine.server.enums.RemoteInvokeTypeEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 远端类型管理器
 * @author liukaixiong
 * @date : 2022/6/9 - 14:16
 */
@Component
@Slf4j
public class RemoteTypeManager {

    @Autowired
    private List<ExpressionRemoteInvoker> remoteInvokerList;

    public ExpressionRemoteInvoker getExpressionRemoteInvoker(RemoteInvokeTypeEnums invokeType) {
        for (ExpressionRemoteInvoker expressionRemoteInvoker : remoteInvokerList) {
            if (invokeType == expressionRemoteInvoker.type()) {
                return expressionRemoteInvoker;
            }
        }
        // 没有找到对应的执行器
        log.warn("沒有找到对应远端执行器实现：{}", invokeType.name());
        return null;
    }


}
