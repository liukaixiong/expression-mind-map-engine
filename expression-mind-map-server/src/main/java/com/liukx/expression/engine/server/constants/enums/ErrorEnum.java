package com.liukx.expression.engine.server.constants.enums;

import lombok.Getter;

@Getter
public enum ErrorEnum {
    /**
     * Params error error enum.
     */
    PARAMS_ERROR(100400, "参数错误"),
    OPERATOR_ERROR(100401, "操作异常"),
    SYSTEM_ERROR(100500, "系统繁忙,请稍后再试"),
    DATA_ERROR(100100, "数据异常:[%s]"),
    SYSTEM_SERVICE_ERROR(100503, "內部服务调用异常"),

    VARIABLE_NOT_FOUND(100100, "变量没有找到对应的解释器"),

    EXECUTOR_NOT_FOUND(1001001, "找不到对应的执行器配置"),
    EXPRESSION_ILLEGAL_TYPE_ADD(200001, "表达式配置，不合法的表达式类型"),
    REPEATED_EXPRESSION_ADD(200002, "表达式编码已存在，请勿重复添加"),
    EXPRESSION_CONTENT_NULL(200003, "表达式内容为空,请重新添加"),
    EXPRESSION_CODE_NULL(200004, "表达式编码不能为空,请重新添加（需唯一）"),
    ADD_TO_DB_ERROR(500001, "数据入库失败，请联系管理员"),
    REPEATED_ADD_DB(500002, "该数据已经存在"),
    UPDATE_TO_DB_ERROR(500003, "数据更新到数据库发生错误，请联系管理员"),
    UPDATE_NOT_EXIST_DATA(500004, "找不到指定id的数据，无法完成更新操作!"),
    NON_EXIST_SERVICE_NAME(500005, "服务名称不存在"),
    REPEATED_EXECUTOR_NAME_ADD(600001, "执行器已存在，请勿重复添加"),
    REPEATED_EXECUTOR_BUSINESS_CODE_ADD(600002, "执行器的业务编码已存在，请勿重复添加"),
    ;

    private final String message;
    private final Integer code;

    ErrorEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Message string.
     *
     * @return the string
     */
    public String message() {
        return message;
    }

    /**
     * Code int.
     *
     * @return the int
     */
    public Integer code() {
        return code;
    }

}
