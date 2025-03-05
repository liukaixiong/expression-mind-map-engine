package com.liukx.expression.engine.server.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 *
 *
 * @author liukaixiong
 * @date 2025/2/26 - 13:39
 */
@ConfigurationProperties(prefix = "spring.expression.server")
@Data
@RefreshScope
public class ExpressionServerProperties {
    /**
     * 是否启用登录
     */
    private boolean enableLogin;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

}
