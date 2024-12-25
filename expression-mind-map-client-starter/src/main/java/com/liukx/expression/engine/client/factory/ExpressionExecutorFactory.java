package com.liukx.expression.engine.client.factory;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.process.AbstractExpressionService;
import com.liukx.expression.engine.client.process.AviatorEvaluatorServiceImpl;
import com.liukx.expression.engine.core.api.model.ExpressionService;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.enums.ExpressionVariableTypeEnums;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ExpressionExecutorFactory implements InitializingBean, ApplicationContextAware, ExpressFunctionDocumentLoader {
    private final Logger LOG = getLogger(ExpressionExecutorFactory.class);

    private final Map<String, ExpressionService> expressionProcessCache = new HashMap<>();

    private ApplicationContext applicationContext;

    public ExpressionService getExpressionService(String groupName) {
        return expressionProcessCache.get(groupName);
    }

    public ExpressionService getExpressionService() {
        return expressionProcessCache.get(ExpressionVariableTypeEnums.DEFAULT.name());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 注册默认的实现
        expressionProcessCache.computeIfAbsent(ExpressionVariableTypeEnums.DEFAULT.name(), key -> newExpressionService());
    }

    protected AbstractExpressionService newExpressionService() {
        // todo 待优化,希望从全局找寻一个可配置的实现类
        return new AviatorEvaluatorServiceImpl(this.applicationContext);
    }

    @Override
    public List<FunctionApiModel> loadFunctionList() {
        final ExpressionService expressionService = expressionProcessCache.get(ExpressionVariableTypeEnums.DEFAULT.name());
        if (expressionService instanceof ExpressFunctionDocumentLoader) {
            return ((ExpressFunctionDocumentLoader) expressionService).loadFunctionList();
        }
        return new ArrayList<>();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
