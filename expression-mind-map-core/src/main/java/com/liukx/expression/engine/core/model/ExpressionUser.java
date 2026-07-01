package com.liukx.expression.engine.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一用户身份模型
 *
 * @author liukaixiong
 */
public class ExpressionUser {

    private String userId;
    private String username;
    private String source;
    private Map<String, Object> attributes;

    public ExpressionUser() {
    }

    public ExpressionUser(String userId, String username, String source) {
        this.userId = userId;
        this.username = username;
        this.source = source;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public ExpressionUser addAttribute(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "ExpressionUser{userId='" + userId + "', username='" + username + "', source='" + source + "'}";
    }

    /**
     * 用户来源枚举
     */
    public static final class Source {
        public static final String CONSOLE = "CONSOLE";
        public static final String API = "API";
        public static final String EXTERNAL = "EXTERNAL";

        private Source() {
        }
    }
}
