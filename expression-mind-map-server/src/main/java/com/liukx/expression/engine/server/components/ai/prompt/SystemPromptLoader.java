package com.liukx.expression.engine.server.components.ai.prompt;

/**
 * 系统提示词模板加载器
 */
public interface SystemPromptLoader {

    /**
     * 加载基础模板内容
     */
    String loadBase();

    /**
     * 加载指定服务的专属提示词片段，不存在时返回 null
     */
    String loadServicePrompt(String serviceName);
}
