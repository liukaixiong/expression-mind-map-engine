package com.liukx.expression.engine.server;

import cn.hutool.extra.spring.EnableSpringUtil;
import com.liukx.expression.engine.server.mapper.ExpressionConfigMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

/**
 -javaagent:D:\lib\opentelemetry-javaagent.jar
 -Dotel.traces.exporter=otlp
 -Dotel.exporter.otlp.endpoint=http://127.0.0.1:4317
 -Dotel.exporter.otlp.protocol=grpc
 -Dio.opentelemetry.javaagent.debug=true
 -Dotel.traces.sampler=parentbased_always_on
 -Dapp.nacos.ip=127.0.0.1:8848
 -Dapp.nacos.enable=true
 -Dspring.expression.server.enable-login=false
 -Dlogging.level.com.liukx.expression=debug
 */
@MapperScan(basePackageClasses = ExpressionConfigMapper.class)
@SpringBootApplication
@EnableDiscoveryClient
@EnableSpringUtil
//@EnableScheduling
@Import({RedisAutoConfiguration.class})
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }

}
