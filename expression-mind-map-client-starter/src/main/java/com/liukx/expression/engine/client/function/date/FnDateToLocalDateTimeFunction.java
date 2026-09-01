package com.liukx.expression.engine.client.function.date;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Date 转换成 LocalDateTime
 * @author liukaixiong
 * @date 2025/5/19 - 18:33
 */
@Component
public class FnDateToLocalDateTimeFunction extends AbstractSimpleFunction {

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.SYS_DATE_DAY_TO_LOCAL_DATE_TIME;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final Date argsIndexDate = getArgsIndexDate(funArgs, 0, new Date());
        return argsIndexDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
