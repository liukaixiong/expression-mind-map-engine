package com.liukx.expression.engine.server.service;

import cn.hutool.core.lang.tree.Tree;
import com.liukx.expression.engine.server.enums.SyncDataEnums;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorInfoConfig;

import java.util.List;
import java.util.Map;

/**
 * @author liukaixiong
 * @date 2024/2/20
 */
public interface SyncDataService<T> {

    public SyncDataEnums syncType();

    public boolean importData(T data);

    void refreshImportNode(List<ExpressionExecutorInfoConfig> nodeInfo, Long executorId, List<Tree<Long>> treeList, Map<String, ExpressionExecutorInfoConfig> dbCodeMap, Map<Long, Long> idCache);

    public T export(Long id);

}
