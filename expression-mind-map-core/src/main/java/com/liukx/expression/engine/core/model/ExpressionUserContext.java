package com.liukx.expression.engine.core.model;

/**
 * 基于 ThreadLocal 的用户上下文，服务端和客户端均可使用
 *
 * @author liukaixiong
 */
public class ExpressionUserContext {

    /**
     * 系统默认账号：未开启登录（enable-login=false）时的兜底操作人。
     * 用于保证审计字段（creator/updater/createBy/updateBy）与历史版本 operator 非空。
     */
    public static final String SYSTEM_OPERATOR = "system";

    private static final ThreadLocal<ExpressionUser> HOLDER = new ThreadLocal<>();

    public static void set(ExpressionUser user) {
        HOLDER.set(user);
    }

    public static ExpressionUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前用户名，无用户时返回 null
     */
    public static String currentUsername() {
        ExpressionUser user = HOLDER.get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户ID，无用户时返回 null
     */
    public static String currentUserId() {
        ExpressionUser user = HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前操作人：登录时返回真实 username；未登录返回 {@link #SYSTEM_OPERATOR}。
     * 用于所有审计字段填充与历史版本 operator，保证非空且来源可信（token / 上下文）。
     */
    public static String currentUsernameOrSystem() {
        String username = currentUsername();
        return username != null ? username : SYSTEM_OPERATOR;
    }

    public static void clear() {
        HOLDER.remove();
    }

    private ExpressionUserContext() {
    }
}
