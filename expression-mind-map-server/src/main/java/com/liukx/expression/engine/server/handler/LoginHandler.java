package com.liukx.expression.engine.server.handler;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.core.model.ExpressionUserContext;
import com.liukx.expression.engine.jdk.handler.AbstractLoginHandler;
import com.liukx.expression.engine.jdk.utils.ServletUtil;
import com.liukx.expression.engine.server.auth.IdentityExtractor;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.constants.BaseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoginHandler extends AbstractLoginHandler {

    private final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Autowired
    private ExpressionServerProperties serverProperties;

    @Autowired
    private List<IdentityExtractor> extractors;

    @Override
    public boolean preHandle0(Object request, Object response, Object handler) throws Exception {
        logger.debug(ServletUtil.getRequestURI());

        if (!serverProperties.isEnableLogin()) {
            return true;
        }

        for (IdentityExtractor extractor : extractors) {
            if (extractor.supports()) {
                ExpressionUser user = extractor.extract();
                if (user != null) {
                    ExpressionUserContext.set(user);
                    return true;
                }
            }
        }

        logger.debug("身份校验失败");

        boolean isApiRequest = ServletUtil.isXmlHttpRequest()
                || (ServletUtil.getAcceptHeader() != null && ServletUtil.getAcceptHeader().contains("application/json"));

        if (isApiRequest) {
            sendUnauthorized(response);
            return false;
        }

        // 跳转登录页时携带原始请求地址，便于登录成功后回到过期前的页面
        String targetPath = buildLoginRedirect();
        sendRedirect(response, targetPath);
        return false;
    }

    /**
     * 构造登录页跳转地址，附带原始请求 URI + query string 作为 redirect 参数。
     * 例如: /template/login.html?redirect=%2Ftemplate%2Fexpression-rule-config.html%3Fid%3D1
     */
    private String buildLoginRedirect() {
        String origin = ServletUtil.getRequestUrlWithQuery();
        String encoded = ServletUtil.urlEncode(origin);
        return BaseConstants.HTML_LOGIN_PATH + "?redirect=" + encoded;
    }

    @Override
    public void afterCompletion0(Object request, Object response, Object handler, Exception ex) {
        ExpressionUserContext.clear();
    }
}
