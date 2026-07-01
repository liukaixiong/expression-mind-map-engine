package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.server.model.dto.request.LoginModel;

/**
 * 登录逻辑
 *
 * @author liukaixiong
 * @date 2024/10/15 - 13:25
 */
public interface IExpressionLoginService {

    boolean login(LoginModel loginModel);

    /**
     * 认证并返回用户详情
     *
     * @param loginModel 登录信息
     * @return 用户身份，认证失败返回 null
     */
    ExpressionUser authenticate(LoginModel loginModel);
}
