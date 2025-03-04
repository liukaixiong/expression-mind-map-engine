package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.server.model.dto.request.LoginModel;

/**
 * @author liukaixiong
 * @date 2024/10/15 - 13:33
 */
public interface IExpressionTokenService {

    public String tokenCreated(LoginModel loginModel);

    public boolean checkToken(String token);

}
