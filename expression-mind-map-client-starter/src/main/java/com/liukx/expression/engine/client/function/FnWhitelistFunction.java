package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.api.ListCheckService;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 白名单操作函数
 * <p>
 * 默认为查询操作（contains），可通过第4个参数指定操作类型：add=新增，del=删除。
 *
 * @author liukaixiong
 * @date 2026/5/14
 */
@Component
public class FnWhitelistFunction extends AbstractSimpleFunction {

    @Autowired
    private ListCheckService listCheckService;

    @Override
    protected boolean isAllowedCache() {
        return false;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.WHITELIST;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel,
                            ExpressionBaseRequest request, List<Object> funArgs) {
        String group = getConvertValue(funArgs, 0, String.class);
        String key = getConvertValue(funArgs, 1, String.class);
        String value = getConvertValue(funArgs, 2, String.class);
        String operation = getConvertValue(funArgs, 3, String.class, "contains");

        Object result;
        switch (operation) {
            case "add":
                result = listCheckService.add(group, key, value);
                env.recordTraceDebugContent(getName(), "debug",
                        String.format("白名单新增: group=%s, key=%s, value=%s, result=%s", group, key, value, result));
                break;
            case "del":
                result = listCheckService.remove(group, key, value);
                env.recordTraceDebugContent(getName(), "debug",
                        String.format("白名单删除: group=%s, key=%s, value=%s, result=%s", group, key, value, result));
                break;
            default:
                result = listCheckService.contains(group, key, value);
                env.recordTraceDebugContent(getName(), "debug",
                        String.format("白名单检查: group=%s, key=%s, value=%s, result=%s", group, key, value, result));
                break;
        }
        return result;
    }
}
