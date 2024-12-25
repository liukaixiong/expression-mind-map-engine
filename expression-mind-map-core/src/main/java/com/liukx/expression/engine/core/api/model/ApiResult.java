package com.liukx.expression.engine.core.api.model;

import java.io.Serializable;

/**
 * @author liukx
 */
public class ApiResult<T> implements Serializable {
    private T data;
    private Integer code;
    private String traceLogId;
    private String message;

    public ApiResult() {
    }

    private ApiResult(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    private ApiResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(200, "SUCCESS");
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(200, data, "SUCCESS");
    }

    public static <T> ApiResult<T> ok(T data, String message) {
        return new ApiResult<>(200, data, message);
    }

    public static <T> ApiResult<T> error() {
        return new ApiResult<>(500, "ERROR");
    }

    public static <T> ApiResult<T> error(Integer code, String message) {
        return new ApiResult<>(code, null, message);
    }

    public String getTraceLogId() {
        return traceLogId;
    }

    public void setTraceLogId(String traceLogId) {
        this.traceLogId = traceLogId;
    }

    public boolean isOk() {
        return 200 == getCode();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiResult [code=" + code + ", data=" + data + ", message=" + message + "]";
    }

}
