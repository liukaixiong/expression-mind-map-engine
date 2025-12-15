package com.liukx.expression.engine.client.enums;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;

/**
 * 流程控制标记
 *
 * @author liukaixiong
 * @date 2025/9/22 - 19:14
 */
public enum FlowControlEnum {

    FORCE_END(BaseFunctionDescEnum.END_FORCE, "强制结束整个上下文流程"),
    IN_END(BaseFunctionDescEnum.END_IN, "进入该分支后，结束整个上下文流程"),
    RETURN_END(BaseFunctionDescEnum.END_RETURN, "执行完当前分支之后,不在往下执行同级别分支流程"),
    //    IN_RETURN_END(BaseFunctionDescEnum.END_IN_RETURN, "执行完当前分支之后,不在往下执行同级别分支流程"),
    GO_ON(null, "继续");

    FlowControlEnum(BaseFunctionDescEnum descEnum, String remark) {

    }
}
