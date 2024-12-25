package com.liukx.expression.engine.server.exception;

import com.liukx.expression.engine.server.constants.enums.ErrorEnum;

import java.util.Objects;


public class Throws {

    private Throws() {
    }

    public static BusinessException error(Integer code, String message) {
        throw new BusinessException(code, message);
    }

    public static BusinessException error(String message) {
        throw new BusinessException(500, message);
    }

    public static void error(ErrorEnum errorEnum) {
        throw new BusinessException(errorEnum.code(), errorEnum.message());
    }

    public static void nullError(ErrorEnum errorEnum, Object object) {
        if (Objects.isNull(object)) {
            error(errorEnum);
        }
    }

    public static void falseError(ErrorEnum errorEnum, boolean flag) {
        if (!flag) {
            error(errorEnum);
        }
    }

    public static void boolError(boolean test, ErrorEnum errorEnum) {
        falseError(errorEnum, !test);
    }

    public static void remoteInvokeError(boolean bool, String serverName) {
        if (bool) {
            throw new BusinessException(ErrorEnum.SYSTEM_SERVICE_ERROR.code(), "服务调用[" + serverName + "]失败");
        }
    }

    public static void check(boolean check, String message) {
        if (check) {
            throw new BusinessException(ErrorEnum.PARAMS_ERROR.code(), message);
        }
    }

    public static void nullError(Object obj, String field) {
        if (obj == null) {
            throw new BusinessException(ErrorEnum.PARAMS_ERROR.code(), field + " 是必填項！");
        }
    }
}
