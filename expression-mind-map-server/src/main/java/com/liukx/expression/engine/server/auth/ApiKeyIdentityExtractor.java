package com.liukx.expression.engine.server.auth;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.jdk.utils.ServletUtil;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 从 API Key Header 提取用户身份（服务间调用场景）
 *
 * <p>请求头：X-Api-Key
 * <p>需要在配置中设置 <code>spring.expression.server.api-key</code>
 *
 * @author liukaixiong
 */
@Component
@Order(3)
public class ApiKeyIdentityExtractor implements IdentityExtractor {

    private static final String HEADER_API_KEY = "X-Api-Key";

    @Autowired
    private ExpressionServerProperties serverProperties;

    @Override
    public boolean supports() {
        String apiKey = ServletUtil.getHeader(HEADER_API_KEY);
        String configuredKey = serverProperties.getApiKey();
        return StringUtils.hasText(apiKey) && StringUtils.hasText(configuredKey);
    }

    @Override
    public ExpressionUser extract() {
        String apiKey = ServletUtil.getHeader(HEADER_API_KEY);
        String configuredKey = serverProperties.getApiKey();

        if (!StringUtils.hasText(apiKey) || !apiKey.equals(configuredKey)) {
            return null;
        }

        ExpressionUser user = new ExpressionUser();
        user.setUserId("API_CLIENT");
        user.setUsername("API客户端");
        user.setSource(ExpressionUser.Source.API);
        return user;
    }
}
