package com.liukx.expression.engine.server.exception;

import com.liukx.expression.engine.server.constants.enums.ErrorEnum;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(Integer code) {
        super();
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ErrorEnum.PARAMS_ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }


    public BusinessException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
