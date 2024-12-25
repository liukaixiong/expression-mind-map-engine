package com.liukx.expression.engine.client.enums;

/**
 * 表达式配置调用方式
 *
 * @author liukaixiong
 * @date 2024/9/10 - 13:35
 */
public enum ExpressionConfigCallEnum {
    /**
     * 通过远端调用去获取服务端的配置
     */
    http,
    /**
     * 如果引擎服务端和客户端都在一个redis中，可以尝试直接从缓存中获取，省略中间调用部分
     */
    redis
}
