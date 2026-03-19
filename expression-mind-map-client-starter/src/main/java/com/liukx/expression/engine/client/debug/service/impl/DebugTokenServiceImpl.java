package com.liukx.expression.engine.client.debug.service.impl;

import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.client.debug.service.IDebugTokenService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 调试Token校验服务实现
 *
 * @author liukaixiong
 */
public class DebugTokenServiceImpl implements IDebugTokenService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ExpressionProperties properties;

    @Override
    public boolean checkDebugToken(String token) {
        if (!properties.isDebugEnabled()) {
            logger.debug("debug调试未开启!");
            return false;
        }

        String debugToken = properties.getDebugToken();
        if (StringUtils.isBlank(debugToken)) {
            logger.debug("token不匹配!");
            return false;
        }
        return debugToken.equals(token);
    }
}
