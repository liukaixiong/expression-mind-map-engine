package com.liukx.expression.engine.core.consts;

/**
 * 表达式常量值
 *
 * @author liukaixiong
 */
public class ExpressionConstants {
    /**
     * 默认的请求参数名称key
     */
    public static final String PARAMS_REQUEST_KEY = "req";

    public static final String ENGINE_SERVER_ID = "expression-mind-map-server";

    public static final String PATH_EXPRESSION = "/expression/api";

    /**
     * 系统函数文档地址
     */
    public static final String SYSTEM_FUNCTION_DOC_LINK = "https://www.yuque.com/boyan-avfmj/aviatorscript/ashevw";

    /**
     * 执行器
     */
    public static final String PATH_EXPRESSION_EXECUTOR = PATH_EXPRESSION + "/executor";

    /**
     * 变量
     */
    public static final String PATH_EXPRESSION_DOCUMENT = PATH_EXPRESSION + "/document";

    /**
     * 引擎端执行路径
     */
    public static final String SERVER_EXECUTOR_PATH = "/server/executor";
    /**
     * 执行器追踪日志提交
     */
    public static final String SERVER_EXECUTOR_TRACE_SUBMIT_PATH = "/server/trace/log/submit";
    public static final String SERVER_ASYNC_EXECUTOR_PATH = "/server/submit";
    /**
     * 引擎配置内容
     */
    public static final String SERVER_CONFIG_DISCOVERY = "/server/config/discovery";
    /**
     * 客户端文档信息提交
     */
    public static final String SERVER_DOC_SUBMIT = "/server/doc/submit";
}
