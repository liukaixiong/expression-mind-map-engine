package com.liukx.expression.engine.client.config.props;

import com.liukx.expression.engine.client.enums.ExpressionConfigCallEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.plugin.express")
public class ExpressionProperties {


    private Boolean debug = false;

    /**
     * 默认的日志等级
     */
    private String loggerTraceLevel = "DEBUG";

    /**
     * 远端引擎地址
     */
    private String remoteEngineUrl;

    /**
     * 表达式配置的获取方式，通过http请求获取，或者通过redis【服务端和客户端都在一个redis的库中】
     */
    private String expressionConfigCall = ExpressionConfigCallEnum.http.name();

    /**
     * 是否开启表达式追踪
     */
    private boolean enableTraceLog;

    /**
     * 注入变量类型包扫描
     */
    private String injectTypePackage;


    public String getInjectTypePackage() {
        return injectTypePackage;
    }

    public void setInjectTypePackage(String injectTypePackage) {
        this.injectTypePackage = injectTypePackage;
    }

    public String getExpressionConfigCall() {
        return expressionConfigCall;
    }

    public void setExpressionConfigCall(String expressionConfigCall) {
        this.expressionConfigCall = expressionConfigCall;
    }

    public boolean isEnableTraceLog() {
        return enableTraceLog;
    }

    public void setEnableTraceLog(boolean enableTraceLog) {
        this.enableTraceLog = enableTraceLog;
    }

    public String getRemoteEngineUrl() {
        return remoteEngineUrl;
    }

    public void setRemoteEngineUrl(String remoteEngineUrl) {
        this.remoteEngineUrl = remoteEngineUrl;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public String getLoggerTraceLevel() {
        return loggerTraceLevel;
    }

    public void setLoggerTraceLevel(String loggerTraceLevel) {
        this.loggerTraceLevel = loggerTraceLevel;
    }
}
