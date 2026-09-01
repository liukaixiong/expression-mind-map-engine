package com.liukx.expression.engine.client.function.flow;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import org.springframework.stereotype.Component;


/**
 * 描述: 执行当前分支的内部子分支流程后,同级别分支不在继续
 *
 * @author liukx
 * @date 2025/2/13 14:22
 */
@Component
public class FnInReturnFunction extends FnReturnFunction {

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.END_IN_RETURN;
    }
}
