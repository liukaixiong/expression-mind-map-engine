package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.server.enums.SyncDataEnums;

/**
 * @author liukaixiong
 * @date 2024/2/20
 */
public interface SyncDataService<T> {

    public SyncDataEnums syncType();

    public boolean importData(T data);

    public T export(Long id);

}
