package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import org.springframework.stereotype.Component;

/**
 * 基础的函数定义
 *
 * @author liukaixiong
 * @date 2024/8/16 - 13:56
 */
@Component
public class FnInEndFunction extends FnTopEndFunction {
    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.END_IN;
    }
}
