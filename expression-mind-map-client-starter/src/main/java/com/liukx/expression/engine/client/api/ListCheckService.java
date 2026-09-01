package com.liukx.expression.engine.client.api;

/**
 * 名单检查服务接口
 * <p>
 * 用于判断指定值是否存在于名单中，以及新增和删除名单值。
 * 默认提供 Redis 实现，用户可替换为自己的存储实现。
 *
 * @author liukaixiong
 * @date 2026/5/14
 */
public interface ListCheckService {

    /**
     * 检查指定值是否存在于名单中
     *
     * @param group 名单组
     * @param key   名单 key
     * @param value 名单值
     * @return true=存在，false=不存在
     */
    boolean contains(String group, String key, String value);

    /**
     * 新增值到名单中
     *
     * @param group 名单组
     * @param key   名单 key
     * @param value 名单值
     * @return true=操作成功
     */
    boolean add(String group, String key, String value);

    /**
     * 从名单中删除值
     *
     * @param group 名单组
     * @param key   名单 key
     * @param value 名单值
     * @return true=操作成功
     */
    boolean remove(String group, String key, String value);
}
