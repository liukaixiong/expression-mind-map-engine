package com.liukx.expression.engine.client.process;

import com.googlecode.aviator.*;
import com.googlecode.aviator.runtime.function.ClassMethodFunction;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.TranslateResult;
import com.liukx.expression.engine.core.api.model.ValidatorResult;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import com.liukx.expression.engine.core.model.ContextTemplateRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AviatorEvaluatorServiceImpl extends AbstractExpressionService implements EnvProcessor, FunctionLoader, ExpressFunctionDocumentLoader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AviatorEvaluatorInstance evaluator = AviatorEvaluator.newInstance();

    private final ApplicationContext applicationContext;
    private final ExpressionVariableManager variableDefinition;

    public AviatorEvaluatorServiceImpl(ApplicationContext applicationContext) {
        super("default");
        this.applicationContext = applicationContext;
        this.variableDefinition = this.applicationContext.getBean(ExpressionVariableManager.class);
        // 当变量找不到的时候，尝试从本地变量去解释
        evaluator.setEnvProcessor(this);
        // 当函数找不到的时候，可能需要去远端查找，这里注册一个相关的函数查找规则
        evaluator.addFunctionLoader(this);
        // 保留metaspace,也就是每个表达式解析对象
        evaluator.setCachedExpressionByDefault(true);
        evaluator.useLRUExpressionCache(10000);
        try {
            evaluator.addStaticFunctions("objectUtils", ObjectUtils.class);
            evaluator.addStaticFunctions("stringUtils", StringUtils.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 初始化本地的函数到上下文中
        initContextFunction();
    }

    @Override
    public List<FunctionApiModel> loadFunctionList() {
        final Map<String, Object> funcMap = evaluator.getFuncMap();
        List<FunctionApiModel> list = new ArrayList<>();
        for (String name : funcMap.keySet()) {
            final Object funMethod = funcMap.get(name);
            if (!(funMethod instanceof AbstractSimpleFunction)) {
                FunctionApiModel def = new FunctionApiModel();
                def.setName(name);
                def.setGroupName("system");
                def.setDescribe("系统函数:" + ExpressionConstants.SYSTEM_FUNCTION_DOC_LINK);
                if (funMethod instanceof ClassMethodFunction) {
                    final Object clazz = getFiledValue(funMethod, "clazz");
                    if (clazz != null) {
                        def.setDescribe("导入函数参考:" + clazz);
                    }
                }
                def.setResultClassType("");
                def.setArgs(Collections.singletonList(""));
                def.setExample(name);
                list.add(def);
            }
        }
        return list;
    }

    private Object getFiledValue(Object funMethod, String fieldName) {
        try {
            return FieldUtils.readDeclaredField(funMethod, fieldName, true);
        } catch (Exception e) {

        }
        return "";
    }

    private void initContextFunction() {
        try {
            // 初始化上下文中所涵盖的所有函数
            final Map<String, AviatorFunction> aviatorFunctionMap = this.applicationContext.getBeansOfType(AviatorFunction.class);
            aviatorFunctionMap.values().forEach(evaluator::addFunction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enableDebug(boolean isEnableDebug) {
        if (isEnableDebug) {
            evaluator.setOption(Options.TRACE_EVAL, true);
            evaluator.setTraceOutputStream(System.out);
            evaluator.setOption(Options.CAPTURE_FUNCTION_ARGS, true);
        }
    }

    @Override
    public void beforeExecute(Map<String, Object> env, Expression script) {

        // 参数转换
        ContextTemplateRequest envRequest = getContextTemplateRequest(env);

        // 获取当前表达式中的所有变量
        List<String> variableFullNames = script.getVariableNames().stream().filter(name -> !name.startsWith(ExpressionConstants.PARAMS_REQUEST_KEY)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(variableFullNames)) {
            return;
        }

        // 排除已经形成的变量
        List<String> varList = variableFullNames.stream().filter(var -> !env.containsKey(var)).collect(Collectors.toList());

        // 需要通过计算得到的变量
        Map<String, Object> variableValueMap = this.variableDefinition.processList(varList, envRequest);

        env.putAll(variableValueMap);
    }

    @Override
    public void afterExecute(Map<String, Object> env, Expression script) {
        // 暂无实现
    }

    @Override
    public AviatorFunction onFunctionNotFound(String functionName) {
        logger.warn("没有找到函数的定义:{}", functionName);
        return null;
    }

    private ContextTemplateRequest getContextTemplateRequest(Map<String, Object> env) {
        final ExpressionEnvContext expressionEnvContext = new ExpressionEnvContext(env);
        ContextTemplateRequest envRequest = new ContextTemplateRequest();
        envRequest.setEnvContext(env);
        envRequest.setRequest(expressionEnvContext.getObject(ExpressionBaseRequest.class));
        return envRequest;
    }

    @Override
    public ValidatorResult validator(String expression) {
        try {
            // 验证语法
            evaluator.validate(expression);
            // 验证参数、函数方法是否存在
            List<String> variableFullNames = evaluator.compile(expression).getVariableFullNames();

            // 验证函数
        } catch (Exception e) {
            logger.warn("表达式:{} , 验证有误 : {}", expression, e.getMessage());
            return ValidatorResult.NO(e.getMessage());
        }

        return ValidatorResult.OK();
    }

    @Override
    public TranslateResult translate(String expression) {

        AtomicReference<String> reference = new AtomicReference<>();
        reference.set(expression);
        AviatorEvaluatorInstance newEvaluator = AviatorEvaluator.newInstance();
        List<FunctionApiModel> functionApiModelList = new ArrayList<>();

        newEvaluator.addFunctionLoader((name) -> {
            FunctionApiModel functionApiInfo = getDocumentApiExecutor().getFunctionApi(name);
            functionApiModelList.add(functionApiInfo);
            String text = reference.get().replaceAll(name, functionApiInfo.getDescribe());
            reference.set(text);
            return null;
        });

        Expression compile = newEvaluator.compile(expression);

        List<VariableApiModel> variableApiModelList = new ArrayList<>();
        for (String variableName : compile.getVariableNames()) {
            VariableApiModel variableApi = getDocumentApiExecutor().getVariableApi(getNameSpace(), variableName);
            if (variableApi != null) {
                String text = reference.get().replaceAll(variableName, variableApi.getDescribe());
                reference.set(text);
                variableApiModelList.add(variableApi);
            }
        }

        TranslateResult result = new TranslateResult();
        result.setSimpleTranslateText(reference.get());
        result.setFunctionApiList(functionApiModelList);
        result.setVariableApiList(variableApiModelList);
        return result;
    }

    @Override
    public Object execute(String expression, Map<String, Object> env) {
        return evaluator.execute(expression, env, true);
    }

}
