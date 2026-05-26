package com.liukx.expression.engine.client.enums;

/**
 * 表达式能力开关定义
 *
 * @author liukaixiong
 * @date 2024/9/11 - 11:19
 */
public enum ExpressionConfigurabilitySwitchEnum {

    /**
     * 开启节点异步能力
     */
    enableNodeAsync,
    /**
     * 采样
     */
    enableSampleBody,
    /**
     * 启用全局锁
     */
    enableGlobalLock,
    /**
     * 启用缓存能力
     */
    enableCache,
    /**
     * 异常跳过能力
     */
    enableExceptionSkip,


}
