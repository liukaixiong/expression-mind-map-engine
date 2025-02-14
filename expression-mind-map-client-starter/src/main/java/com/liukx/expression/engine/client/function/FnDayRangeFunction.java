package com.liukx.expression.engine.client.function;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

        final String startDateStr = getArgsIndexValue(funArgs, 0);

        final String endDateStr = getArgsIndexValue(funArgs, 1);
        DateTime startDateTime;
        DateTime endDateTime;
        if (startDateStr.length() == 10) {
            startDateTime = DateUtil.beginOfDay(DateUtil.parseDate(startDateStr));
            endDateTime = DateUtil.endOfDay(DateUtil.parseDate(endDateStr));
        } else {
            startDateTime = DateUtil.parseDateTime(startDateStr);
            endDateTime = DateUtil.parseDateTime(endDateStr);
        }

        final long currentTime = System.currentTimeMillis();

        boolean result = startDateTime.getTime() < currentTime && currentTime <= endDateTime.getTime();

        if (!result) {
            final String s = DateUtil.formatDateTime(startDateTime);
            final String e = DateUtil.formatDateTime(endDateTime);
            env.recordTraceDebugContent(getName(), "debug", String.format("当前时间不满足 %s - %s", s, e));
            log.info("系统时间不在指定时间范围内,startHour={};endHour={} ", s, e);
        }

        return result;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.SYS_DATE_DAY_RANGE;
    }
}
