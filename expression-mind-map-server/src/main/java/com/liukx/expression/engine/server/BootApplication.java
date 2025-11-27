package com.liukx.expression.engine.server;

import cn.hutool.extra.spring.EnableSpringUtil;
import com.liukx.expression.engine.server.mapper.ExpressionConfigMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

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
