package com.liukx.expression.engine.server.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.liukx.expression.engine.server.config.props.AiExpressionProperties;
import com.liukx.expression.engine.server.service.TraceLogStorageService;
import com.liukx.expression.engine.server.service.impl.storage.DefaultMysqlTraceLogStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @author liukaixiong
 * @date 2023/12/12
 */
@Configuration
public class ServiceConfiguration {

    @Value("${spring.http.connect-timeout:5s}")
    private Duration connectTimeout;

    @Value("${spring.http.read-timeout:10s}")
    private Duration readTimeout;

    //    @Bean(
//            name = {"multipartResolver"}
//    )
//    @ConditionalOnMissingBean({MultipartResolver.class})
//    public StandardServletMultipartResolver multipartResolver() {
//        //        multipartResolver.setResolveLazily(this.multipartProperties.isResolveLazily());
//        return new StandardServletMultipartResolver();
//    }
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        // 添加动态表名插件
//        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor(tableNameHandler);
//        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceLogStorageService traceLogStorageService() {
        return new DefaultMysqlTraceLogStorageService();
    }

}
