package com.liukx.expression.engine.client.factory;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.feature.FunctionContextManager;
import com.liukx.expression.engine.client.process.*;
import com.liukx.expression.engine.core.api.model.ExpressionService;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.enums.ExpressionVariableTypeEnums;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ExpressionExecutorFactory implements InitializingBean, ExpressFunctionDocumentLoader {
    private final Logger LOG = getLogger(ExpressionExecutorFactory.class);

    private final Map<String, ExpressionService> expressionProcessCache = new HashMap<>();

    @Autowired
    private ExpressionVariableManager expressionVariableManager;

    @Autowired
    private List<AbstractSimpleFunction> aviatorFunctionList;

    @Autowired
    private FunctionContextManager functionContextManager;

    @Autowired(required = false)
    private ExpressionConfigureCustomizer configureCustomizer;

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
        final AviatorEvaluatorServiceImpl aviatorEvaluatorService = new AviatorEvaluatorServiceImpl(this.expressionVariableManager, aviatorFunctionList, functionContextManager);
        if (configureCustomizer != null) {
            configureCustomizer.customize(aviatorEvaluatorService);
        }
        return aviatorEvaluatorService;
    }

    @Override
    public List<FunctionApiModel> loadFunctionList() {
        final ExpressionService expressionService = expressionProcessCache.get(ExpressionVariableTypeEnums.DEFAULT.name());
        if (expressionService instanceof ExpressFunctionDocumentLoader) {
            return ((ExpressFunctionDocumentLoader) expressionService).loadFunctionList();
        }
        return new ArrayList<>();
    }

}
