package com.liukx.expression.engine.server.executor;

import com.liukx.expression.engine.core.api.model.ApiResult;
import com.liukx.expression.engine.core.api.model.api.GlobalExpressionDocInfo;
import com.liukx.expression.engine.core.api.model.api.RemoteExpressionModel;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import com.liukx.expression.engine.core.utils.Jsons;
import com.liukx.expression.engine.server.enums.RemoteInvokeTypeEnums;
import com.liukx.expression.engine.server.exception.Throws;
import com.liukx.expression.engine.server.service.NodeService;
import com.liukx.expression.engine.server.service.model.node.NodeServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * 远端节点执行器
 *
 * @author liukaixiong
 * @date : 2022/6/9 - 16:42
 */
@Component
@Slf4j
public class RemoteNodeExecutor {

    @Autowired
    private RemoteTypeManager remoteTypeManager;

    @Autowired
    private NodeService nodeService;

    /**
     * 刷新远端服务名称
     *
     * @param serviceName
     * @return
     */
    public GlobalExpressionDocInfo getApiDocument(String serviceName) throws Exception {

        // 1. 首先根据服务名称取节点服务中查找
        NodeServiceDto nodeInfo = nodeService.getNodeInfo(serviceName);

        // 2. 找到节点详情,根据节点详情查找
        RemoteInvokeTypeEnums remoteInvokeType = nodeInfo.getRemoteInvokeType();
        ExpressionRemoteInvoker expressionRemoteInvoker =
                remoteTypeManager.getExpressionRemoteInvoker(remoteInvokeType);

        String domain = expressionRemoteInvoker.parseDomain(nodeInfo);

        String url = domain + ExpressionConstants.PATH_EXPRESSION_DOCUMENT;

        ApiResult<String> result = expressionRemoteInvoker.invoke(url, null);
        log.debug("执行远端变量函数解释接口：{} ， 结果：{}", url, Jsons.toJsonString(result));

        if (result.isOk()) {
            return Jsons.parseObject(result.getData(), GlobalExpressionDocInfo.class);
        }

        return null;
    }

    /**
     * 负责前往节点进行执行变量和函数的调度
     *
     * @param serviceName
     * @param expressionModel
     * @return
     */
    public Map<String, Object> executor(String serviceName, RemoteExpressionModel expressionModel) throws Exception {

        NodeServiceDto nodeInfo = nodeService.getNodeInfo(serviceName);
        RemoteInvokeTypeEnums remoteInvokeType = nodeInfo.getRemoteInvokeType();
        ExpressionRemoteInvoker expressionRemoteInvoker =
                remoteTypeManager.getExpressionRemoteInvoker(remoteInvokeType);

        String domain = expressionRemoteInvoker.parseDomain(nodeInfo);

        Throws.check(StringUtils.isEmpty(domain), "找不到对应的节点入口!");

        String url = domain + ExpressionConstants.PATH_EXPRESSION_EXECUTOR;

        ApiResult<String> result = expressionRemoteInvoker.invoke(url, expressionModel);

        log.debug("执行远端变量函数解释接口：{} ，入参：{}，结果：{}", url, Jsons.toJsonString(expressionModel), Jsons.toJsonString(result));
        if (result.isOk()) {
            return Jsons.parseMap(result.getData());
        }
        return Collections.emptyMap();
    }

}
