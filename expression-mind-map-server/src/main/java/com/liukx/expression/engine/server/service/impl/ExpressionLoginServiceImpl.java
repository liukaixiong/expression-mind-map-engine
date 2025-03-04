package com.liukx.expression.engine.server.service.impl;

import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.model.dto.request.LoginModel;
import com.liukx.expression.engine.server.service.IExpressionLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author liukaixiong
 * @date 2024/10/15 - 13:26
 */
@Service
public class ExpressionLoginServiceImpl implements IExpressionLoginService {

    @Autowired
    private ExpressionServerProperties properties;

    @Override
    public boolean login(LoginModel loginModel) {
        final String username = properties.getUsername();
        final String password = properties.getPassword();

        if (!(StringUtils.hasText(username) && StringUtils.hasText(password))) {
            return false;
        }

        return username.equals(loginModel.getUsername()) && password.equals(loginModel.getPassword());
    }
}
