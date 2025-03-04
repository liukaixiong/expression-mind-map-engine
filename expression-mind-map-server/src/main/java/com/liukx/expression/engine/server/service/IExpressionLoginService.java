package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.server.model.dto.request.LoginModel;

/**
 * 登录逻辑
 *
 * @author liukaixiong
 * @date 2024/10/15 - 13:25
 */
public interface IExpressionLoginService {


    public boolean login(LoginModel loginModel);


}
