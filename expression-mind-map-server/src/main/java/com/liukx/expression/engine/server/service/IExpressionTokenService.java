package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.server.model.dto.request.LoginModel;

/**
 * Token 服务接口
 *
 * @author liukaixiong
 * @date 2024/10/15 - 13:33
 */
public interface IExpressionTokenService {

    String tokenCreated(LoginModel loginModel);

    boolean checkToken(String token);

    /**
     * 从 token 解析用户身份
     *
     * @param token 令牌
     * @return 用户身份，无法解析时返回 null
     */
    ExpressionUser resolveToken(String token);
}
