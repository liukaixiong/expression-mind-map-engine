package com.liukx.expression.engine.core.api.model;

public class ValidatorResult {

    private boolean success;

    private String message;

    public static ValidatorResult OK() {
        ValidatorResult result = new ValidatorResult();
        result.setSuccess(true);
        return result;
    }

    public static ValidatorResult NO(String message) {
        ValidatorResult result = new ValidatorResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ValidatorResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
