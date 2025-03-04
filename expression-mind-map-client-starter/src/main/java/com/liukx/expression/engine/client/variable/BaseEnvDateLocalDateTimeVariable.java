package com.liukx.expression.engine.client.variable;

import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 获取当前系统时间
 *
 * @code env_date_local_date.year       获取年份
 * @code env_date_local_date.monthValue 获取月份
 * @code env_date_local_date.dayOfMonth 获取月份第几天
 * @code env_date_local_date.hour   获取当前小时数
 * @code env_date_local_date.minute 获取当前分钟数
 * @author liukaixiong
 * @date 2025/2/27 - 15:51
 */
@Component
public class BaseEnvDateLocalDateTimeVariable extends AbstractExpressionVariableContextProcessor {

    @Override
    public Enum<? extends VariableDefinitionalService> variableName() {
        return BaseVariableEnums.env_date_local_date_time;
    }

    @Override
    public Object processor(String name, ExpressionBaseRequest request, Map<String, Object> envContext) {
        return LocalDateTime.now();
    }
}
