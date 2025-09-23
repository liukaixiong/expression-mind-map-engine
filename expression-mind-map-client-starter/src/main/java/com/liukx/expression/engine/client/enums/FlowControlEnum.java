package com.liukx.expression.engine.client.enums;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;

/**
 * 流程控制标记
 *
 * @author liukaixiong
 * @date 2025/9/22 - 19:14
 */
public enum FlowControlEnum {

    FORCE_END(BaseFunctionDescEnum.END_FORCE),
    IN_END(BaseFunctionDescEnum.END_IN),
    RETURN_END(BaseFunctionDescEnum.END_RETURN),
    IN_RETURN_END(BaseFunctionDescEnum.END_IN_RETURN),
    GO_ON(null);

    FlowControlEnum(BaseFunctionDescEnum descEnum) {

    }
}
