package com.liukx.expression.engine.server.service.model.sync;

import com.liukx.expression.engine.server.enums.SyncDataEnums;

/**
 * 数据同步基础数据结构
 *
 * @author liukaixiong
 * @date 2024/2/20
 */
public class BaseSyncData {

    private SyncDataEnums type;
    private Object data;

    public BaseSyncData(SyncDataEnums type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public SyncDataEnums getType() {
        return type;
    }

    public void setType(SyncDataEnums type) {
        this.type = type;
    }
}
