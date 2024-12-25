//package com.liukx.expression.engine.client.process;
//
//import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
//import com.liukx.expression.engine.client.api.ExpressionFunctionRegister;
//import api.model.api.com.liukx.expression.engine.core.FunctionApiModel;
//import model.com.liukx.expression.engine.core.ContextTemplateRequest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.util.CollectionUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public abstract class AbstractExpressionFunctionDefinition implements InitializingBean, ExpressFunctionDocumentLoader {
//
//    private final Logger logger = LoggerFactory.getLogger(getClass());
//
//    private final Map<String, ExpressionFunctionRegister> nameCache = new HashMap<>();
//
//
//
//    private final List<FunctionApiModel> functionApiModelList = new ArrayList<>();
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//    }
//    @Override
//    public List<FunctionApiModel> loadFunctionList() {
//        return functionApiModelList;
//    }
//    protected String getKey(String name) {
//        return functionPrefix() + "." + name;
//    }
//
//    /**
//     * 默认匹配不上的执行方法
//     *
//     * @param name  逻辑名称
//     * @param cache 参数
//     * @return
//     */
//    protected Object defaultNoNameInvoke(String name, ContextTemplateRequest cache) {
//        return null;
//    }
//
//    /**
//     * 异常情况
//     *
//     * @param e
//     * @return
//     * @throws Exception
//     */
//    protected Object defaultExceptionInvoke(Exception e, String name, ContextTemplateRequest cache) {
//        return null;
//    }
//
//
//    /**
//     * 函数前缀
//     *
//     * @return
//     */
//    public abstract String functionPrefix();
//
//
//}
