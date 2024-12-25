package com.liukx.expression.engine.client.api;

/**
 * 远端访问方式
 *
 * @author liukaixiong
 * @date 2023/12/12
 */
public interface RemoteHttpService {

    public <T> T call(String service, String path, Object body, Class<T> clazz);

}
