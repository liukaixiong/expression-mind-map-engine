package com.liukx.expression.engine.client.config;

import com.liukx.expression.engine.client.api.DocumentApiExecutor;
import com.liukx.expression.engine.client.api.ExpressVariableTypeDocumentLoader;
import com.liukx.expression.engine.client.api.ExpressionAsyncThreadExecutor;
import com.liukx.expression.engine.client.api.thread.ExpressionSpringThreadExecutor;
import com.liukx.expression.engine.client.collect.ExecutorTraceCollectIntercept;
import com.liukx.expression.engine.client.collect.ExpressionDocCollect;
import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.client.feature.ExpressionConfigIdFilterSupport;
import com.liukx.expression.engine.client.feature.ExpressionFunctionNameFilterSupport;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.client.log.LogTraceService;
import com.liukx.expression.engine.client.log.Sl4jLogServiceImpl;
import com.liukx.expression.engine.client.process.ExpressDocumentManager;
import com.liukx.expression.engine.client.process.ExpressVariableTypeDocumentLoaderImpl;
import com.liukx.expression.engine.client.process.ExpressionVariableManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(value = {ExpressionProperties.class})
@Import({ExpClientEnginAutoConfiguration.class})
@ComponentScan(basePackages = {"com.liukx.expression.engine.client.function"})
@Slf4j
public class ExpressionConfiguration {

    /**
     * 开启链路追踪日志
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.plugin.express", value = "enable-trace-log", havingValue = "true", matchIfMissing = true)
    public ExecutorTraceCollectIntercept executorTraceCollectIntercept() {
        log.info("expression -> 【启用表达式日志追踪能力】");
        return new ExecutorTraceCollectIntercept();
    }

    /**
     * 配置异步线程池
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ExpressionAsyncThreadExecutor expressionSpringThreadExecutor() {
        return new ExpressionSpringThreadExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExpressionDocCollect expressionDocCollect() {
        return new ExpressionDocCollect();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExpressVariableTypeDocumentLoader expressVariableTypeDocumentLoader() {
        return new ExpressVariableTypeDocumentLoaderImpl();
    }

    @Bean
    public ExpressionExecutorFactory expressionManager() {
        return new ExpressionExecutorFactory();
    }

    @Bean
    @ConditionalOnMissingBean(value = DocumentApiExecutor.class)
    public ExpressDocumentManager expressDocumentManager() {
        return new ExpressDocumentManager();
    }


    @Bean
    @ConditionalOnMissingBean(value = {LogTraceService.class})
    public LogTraceService sl4jLogService() {
        return new Sl4jLogServiceImpl();
    }

    @Bean
    public LogHelper logHelper() {
        return new LogHelper();
    }

    @Bean
    public ExpressionConfigIdFilterSupport expressionConfigIdFilter() {
        return new ExpressionConfigIdFilterSupport();
    }

    @Bean
    public ExpressionFunctionNameFilterSupport expressionFunctionNameFilterSupport() {
        return new ExpressionFunctionNameFilterSupport();
    }

    @Bean
    public ExpressionVariableManager expressionVariableManager() {
        return new ExpressionVariableManager();
    }
}
