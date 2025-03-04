package com.liukx.expression.engine.server.handler;

import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.service.IExpressionTokenService;
import com.liukx.expression.engine.server.util.CookiesUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class LoginHandler implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Autowired
    private ExpressionServerProperties serverProperties;

    @Autowired
    private IExpressionTokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        logger.debug(request.getRequestURI());

        if (!serverProperties.isEnableLogin()) {
            return true;
        }

        Cookie cookieByName = CookiesUtil.getCookieByName(request, BaseConstants.TOKEN_NAME);

        if (cookieByName != null && StringUtils.hasText(cookieByName.getValue())) {
            if (tokenService.checkToken(cookieByName.getValue())) {
                return true;
            }
        }

        logger.debug("token 校验失败, 返回登录页!");
        response.sendRedirect(BaseConstants.HTML_LOGIN_PATH);
        return false;
    }
}
