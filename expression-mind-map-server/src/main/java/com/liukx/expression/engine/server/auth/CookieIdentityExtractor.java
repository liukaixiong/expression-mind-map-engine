package com.liukx.expression.engine.server.auth;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.jdk.utils.ServletUtil;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.service.IExpressionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 从 Cookie 中提取用户身份（后台管理页面场景）
 *
 * @author liukaixiong
 */
@Component
@Order(1)
public class CookieIdentityExtractor implements IdentityExtractor {

    @Autowired
    private IExpressionTokenService tokenService;

    @Override
    public boolean supports() {
        String token = ServletUtil.getCookieByName(BaseConstants.TOKEN_NAME);
        return StringUtils.hasText(token);
    }

    @Override
    public ExpressionUser extract() {
        String token = ServletUtil.getCookieByName(BaseConstants.TOKEN_NAME);
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return tokenService.resolveToken(token);
    }
}
