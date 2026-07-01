package com.liukx.expression.engine.server.auth;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.jdk.utils.ServletUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 从 HTTP Header 中提取用户身份（外部 Web API 调用场景）
 *
 * <p>请求头约定：
 * <ul>
 *   <li>X-User-Id: 用户唯一标识</li>
 *   <li>X-User-Name: 用户显示名称</li>
 * </ul>
 *
 * <p>注意：此提取器信任调用方已做过鉴权，适用于内部服务间调用或网关已鉴权的场景。
 *
 * @author liukaixiong
 */
@Component
@Order(2)
public class HeaderIdentityExtractor implements IdentityExtractor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";

    @Override
    public boolean supports() {
        String userId = ServletUtil.getHeader(HEADER_USER_ID);
        return StringUtils.hasText(userId);
    }

    @Override
    public ExpressionUser extract() {
        String userId = ServletUtil.getHeader(HEADER_USER_ID);
        String userName = ServletUtil.getHeader(HEADER_USER_NAME);

        if (!StringUtils.hasText(userId)) {
            return null;
        }

        ExpressionUser user = new ExpressionUser();
        user.setUserId(userId);
        user.setUsername(StringUtils.hasText(userName) ? userName : userId);
        user.setSource(ExpressionUser.Source.EXTERNAL);
        return user;
    }
}
