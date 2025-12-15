package com.liukx.expression.engine.server.handler;

import com.liukx.expression.engine.jdk.handler.AbstractLoginHandler;
import com.liukx.expression.engine.jdk.utils.ServletUtil;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.service.IExpressionTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoginHandler extends AbstractLoginHandler {

    private final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Autowired
    private ExpressionServerProperties serverProperties;

    @Autowired
    private IExpressionTokenService tokenService;

    @Override
    public boolean preHandle0(Object request, Object response, Object handler) throws Exception {
        logger.debug(ServletUtil.getRequestURI());

        if (!serverProperties.isEnableLogin()) {
            return true;
        }

        String cookieValue = ServletUtil.getCookieByName(BaseConstants.TOKEN_NAME);

        if (StringUtils.hasText(cookieValue)) {
            if (tokenService.checkToken(cookieValue)) {
                return true;
            }
        }

        logger.debug("token 校验失败, 返回登录页!");

        sendRedirect(response, BaseConstants.HTML_LOGIN_PATH);
        return false;
    }


}
