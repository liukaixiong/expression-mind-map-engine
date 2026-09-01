package com.liukx.expression.engine.client.debug.service;

/**
 * 调试Token校验服务
 *
 * @author liukaixiong
 */
public interface IDebugTokenService {

    /**
     * 校验调试Token是否正确
     *
     * @param token 调试Token
     * @return true-验证成功, false-验证失败
     */
    boolean checkDebugToken(String token);
}
