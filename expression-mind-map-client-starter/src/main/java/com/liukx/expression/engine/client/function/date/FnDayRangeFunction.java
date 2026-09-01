package com.liukx.expression.engine.client.function.date;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import cn.hutool.core.date.DateUtil;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 系统时间小时范围
 *
 * @author liukaixiong
 * @date 2024/8/15 - 11:44
 */
@Component
@Slf4j
public class FnDayRangeFunction extends AbstractSimpleFunction {
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {

        final Date startDateTime = getArgsIndexDate(funArgs, 0);

        final Date endDateTime = getArgsIndexDate(funArgs, 1);

        // 获取时间类型参数，允许是String、Date、Long等等，默认是当前时间来进行计算。
        final Date currentConfigDate = getArgsIndexDate(funArgs, 2, new Date());

        final long currentTime = currentConfigDate.getTime();

        boolean result = startDateTime.getTime() < currentTime && currentTime <= endDateTime.getTime();

        if (!result) {
            final String s = DateUtil.formatDateTime(startDateTime);
            final String e = DateUtil.formatDateTime(endDateTime);
            final String c = DateUtil.formatDateTime(currentConfigDate);
            env.recordTraceDebugContent(getName(), "debug", String.format("配置时间[%s]不满足 %s ~ %s 区间", c, s, e));
        }


        return result;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.SYS_DATE_DAY_RANGE;
    }
}
