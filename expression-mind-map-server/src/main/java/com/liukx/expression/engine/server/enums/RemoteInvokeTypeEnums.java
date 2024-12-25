package com.liukx.expression.engine.server.enums;

import com.liukx.expression.engine.server.components.html.DataRender;

public enum RemoteInvokeTypeEnums implements DataRender {

    HTTP("http调用"),
    INTERNAL("内部调用");

    private String label;

    RemoteInvokeTypeEnums(String describe) {
        this.label = describe;
    }

    @Override
    public Object getCode() {
        return this.name();
    }

    @Override
    public String getLabel() {
        return this.label;
    }
}
