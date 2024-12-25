package com.liukx.expression.engine.server.constants.enums;

public enum ResponseCodeEnum {
    /*
     * 错误信息
     * */
    E_200(200, "操作成功"),
    E_201(201, "对象创建成功"),
    E_202(202, "请求已经被接受"),
    E_400(400, "参数列表错误（缺少参数，格式不匹配）"),
    E_401(401, "未授权,禁止访问"),
    E_403(403, "访问受限，授权过期"),
    E_404(404, "资源，服务未找到"),
    E_500(500, "发生了错误");

    private final Integer code;
    private final String msg;

    ResponseCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
