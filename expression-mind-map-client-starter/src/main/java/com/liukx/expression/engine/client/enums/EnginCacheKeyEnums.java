package com.liukx.expression.engine.client.enums;

import com.liukx.expression.engine.core.consts.ExpressionConstants;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

/**
 * @author liukaixiong
 * @date 2024/2/20
 */
public enum EnginCacheKeyEnums {

    EXECUTOR_REFRESH_KEY("client_query_config", null),
    EXPRESSION_DOC_KEY("expression_doc", null),
    EXPRESSION_VAR_TYPE_KEY("expression_var_type", null),


    ;

    private final String prefix;
    private final Duration timeOut;

    EnginCacheKeyEnums(String prefix, Duration timeOut) {
        this.prefix = prefix;
        this.timeOut = timeOut;
    }

    public Duration getTimeOut() {
        return timeOut;
    }

    public String generateKey(String... key) {
        String prefixString = StringUtils.joinWith(":", ExpressionConstants.ENGINE_SERVER_ID, this.prefix);
        String keys = StringUtils.join(key, ":");
        return prefixString + ":" + keys;
    }
}
