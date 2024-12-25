package com.liukx.expression.engine.client.collect;

import com.liukx.expression.engine.client.api.ExpressVariableTypeDocumentLoader;
import com.liukx.expression.engine.client.api.RemoteHttpService;
import com.liukx.expression.engine.client.process.ExpressDocumentManager;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.api.model.api.GlobalExpressionDocInfo;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import com.liukx.expression.engine.core.enums.ExpressionVariableTypeEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 表达式文档收集器
 * @author liukaixiong
 * @date 2024/11/13 - 14:39
 */
public class ExpressionDocCollect implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(ExpressionDocCollect.class);
    @Autowired
    private ExpressDocumentManager expressDocumentManager;
    @Autowired
    private RemoteHttpService remoteHttpService;
    @Autowired
    private ExpressVariableTypeDocumentLoader variableTypeDocumentLoader;

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    public void pushCollect() {

        if (ExpressionConstants.ENGINE_SERVER_ID.equals(serviceName)) {
            return;
        }

        try {
            List<FunctionApiModel> allFunctionApi = expressDocumentManager.getAllFunctionApi();
            // 获取默认配置的相关变量
            List<VariableApiModel> allVariableApi = expressDocumentManager.getGroupVariableApi(ExpressionVariableTypeEnums.DEFAULT.name());
            final List<VariableApiModel> typeList = variableTypeDocumentLoader.loadList();
            GlobalExpressionDocInfo docInfo = new GlobalExpressionDocInfo();
            docInfo.setServiceName(serviceName);
            docInfo.setFunctionApi(allFunctionApi);
            docInfo.setVariableApi(allVariableApi);
            docInfo.setVariableTypeApi(typeList);
            String obj = remoteHttpService.call(ExpressionConstants.ENGINE_SERVER_ID, ExpressionConstants.SERVER_DOC_SUBMIT, docInfo, String.class);
            logger.debug("【引擎推送服务端文档】 函数:{}个, 变量:{}个 ,结果: ｛｝", allFunctionApi.size(), allVariableApi.size(), obj);
        } catch (Exception e) {
            logger.warn("引擎推送服务端文档失败:{}", e.getMessage());
        }
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        CompletableFuture.runAsync(this::pushCollect);
    }
}
