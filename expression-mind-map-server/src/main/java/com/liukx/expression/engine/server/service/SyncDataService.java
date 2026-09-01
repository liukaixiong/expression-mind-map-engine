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

    /**
     * 刷新导入节点
     *
     * @param nodeInfo     当前节点信息
     * @param executorId   执行器编号
     * @param treeList     导入的节点信息
     * @param dbCodeMap    表达式编码映射关系
     * @param idCache      表达式编号的映射关系
     * @param isExcessData 是否处理多余数据 , 一般全量导入时为true,局部导入时为false
     */
    void refreshImportNode(List<ExpressionExecutorInfoConfig> nodeInfo, Long executorId, List<Tree<Long>> treeList, Map<String, ExpressionExecutorInfoConfig> dbCodeMap, Map<Long, Long> idCache, boolean isExcessData);

    public T export(Long id);

}
