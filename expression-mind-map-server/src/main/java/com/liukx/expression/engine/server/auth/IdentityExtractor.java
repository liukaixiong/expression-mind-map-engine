package com.liukx.expression.engine.server.auth;

import com.liukx.expression.engine.core.model.ExpressionUser;

/**
 * 身份提取策略接口，支持多种认证方式扩展
 *
 * @author liukaixiong
 */
public interface IdentityExtractor {

    /**
     * 是否能处理当前请求（通过 ServletUtil 获取请求信息）
     */
    boolean supports();

    /**
     * 从当前请求中提取用户身份
     */
    ExpressionUser extract();

    /**
     * 优先级，数值越小优先级越高
     */
    default int order() {
        return 0;
    }
}
