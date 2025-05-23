package com.liukx.expression.engine.server.service.impl.syncData;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.liukx.expression.engine.server.enums.SyncDataEnums;
import com.liukx.expression.engine.server.event.ExecutorConfigRefreshEvent;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorInfoConfig;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorBaseDTO;
import com.liukx.expression.engine.server.service.ExpressionConfigService;
import com.liukx.expression.engine.server.service.ExpressionExecutorConfigService;
import com.liukx.expression.engine.server.service.SyncDataService;
import com.liukx.expression.engine.server.service.model.sync.ExpressionExecutorSyncData;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author liukaixiong
 * @date 2024/2/20
 */
@Service
public class ExpressionConfigSyncDataServiceImpl implements SyncDataService<ExpressionExecutorSyncData> {
    private final Logger LOG = LoggerFactory.getLogger(ExpressionConfigSyncDataServiceImpl.class);
    @Autowired
    private ExpressionConfigService expressionConfigService;
    @Autowired
    private ExpressionExecutorConfigService executorConfigService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public SyncDataEnums syncType() {
        return SyncDataEnums.EXPRESSION_EXECUTOR;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public boolean importData(ExpressionExecutorSyncData data) {

        ExpressionExecutorBaseInfo baseInfo = data.getBaseInfo();
        List<ExpressionExecutorInfoConfig> nodeInfo = data.getNodeInfo();
        String serviceName = baseInfo.getServiceName();
        String businessCode = baseInfo.getBusinessCode();
        final String executorCode = baseInfo.getExecutorCode();

        // 如果不应用导入的id,那么优先清空掉所有id.
        ExpressionExecutorBaseDTO expressionExecutorBaseDTO = executorConfigService.queryExecutorInfo(serviceName, businessCode, executorCode);

        // 构建树形结构
        List<Tree<Long>> treeList = TreeUtil.build(nodeInfo, 0L, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setName(treeNode.getExpressionTitle());
            tree.putExtra("obj", treeNode);
        });

        Map<Long, Long> idCache = new HashMap<>(nodeInfo.size() * 2);
        idCache.put(0L, 0L);

        // 如果没有执行器信息，那么说明可以直接覆盖当前执行器和表达式配置信息
        if (expressionExecutorBaseDTO == null) {
            if (baseInfo.getId() != null) {
                LOG.info(">>>>> sync data info insert >>> executorId 存在 : {} 尝试清理!", baseInfo.getId());
                baseInfo.setId(null);
            }
            boolean save = executorConfigService.save(baseInfo);
            Long executorId = baseInfo.getId();
            LOG.info(">>>>> sync data info insert >>> executorId : {} ", executorId);
            if (save) {
                deepInfoConfigSave(treeList, idCache, executorId);
                return true;
            }
        } else {
            // 强制关联已经匹配出来的id信息，直接覆盖，避免不一致
            baseInfo.setId(expressionExecutorBaseDTO.getId());
            Long executorId = expressionExecutorBaseDTO.getId();

            LOG.info(">>>>> sync data info update >>> executorId : {} ", executorId);

            executorConfigService.updateById(baseInfo);

            // 从数据库中获取该执行器的所有表达式信息
            List<ExpressionExecutorInfoConfig> expressionListByBaseId = expressionConfigService.getExpressionListByBaseId(executorId);
            Map<String, ExpressionExecutorInfoConfig> dbCodeMap = expressionListByBaseId.stream().collect(Collectors.toMap(ExpressionExecutorInfoConfig::getExpressionCode, Function.identity()));
            Set<String> hitCode = new HashSet<>();

            if (CollectionUtils.isNotEmpty(nodeInfo)) {
                // 与导入数据进行对比
                deepUpdateConfigInfo(treeList, executorId, dbCodeMap, hitCode, idCache);
                // 多余的数据处理
                excessDataProcessor(dbCodeMap, hitCode);
            }
            // 刷新缓存
            this.eventPublisher.publishEvent(new ExecutorConfigRefreshEvent(expressionExecutorBaseDTO.getId()));
            return true;
        }

        return false;
    }

    /**
     * 递归修改配置信息
     *
     * @param treeList      导入的数据形成的树结构
     * @param executorId    执行器编号
     * @param dbCodeMap     数据库编码的映射表
     * @param hitCode       命中编码表
     * @param idCache       上级id关联表
     */
    private void deepUpdateConfigInfo(List<Tree<Long>> treeList, Long executorId, Map<String, ExpressionExecutorInfoConfig> dbCodeMap, Set<String> hitCode, Map<Long, Long> idCache) {
        // 比如从其他环境导出的数据,要导入当前环境的数据,可能会出现id关联不上,所以这里需要将导出的id和导入的id进行映射,方便到时候进行转换
        if (treeList != null) {
            for (Tree<Long> tree : treeList) {
                ExpressionExecutorInfoConfig importInfoConfig = (ExpressionExecutorInfoConfig) tree.get("obj");
                // 统一优化绑定最新的执行器编号
                importInfoConfig.setExecutorId(executorId);
                // 老的导入的表达式id
                Long oldId = importInfoConfig.getId();
                // 比较因子,目前是以表达式编码为因子关系 , 一旦编码匹配不上，则认为是新增的表达式。
                String expressionCode = importInfoConfig.getExpressionCode();
                ExpressionExecutorInfoConfig expressionExecutorDetailConfig = dbCodeMap.get(expressionCode);
                hitCode.add(expressionCode);

                // 如果存在,走修改的逻辑,修改就是以当前环境查询出来的数据为准,相关id进行替换
                if (expressionExecutorDetailConfig != null) {
                    LOG.debug("trigger update , executorId : {} , expression code : {} - {}", executorId, importInfoConfig.getExpressionType(), expressionCode);
                    // 覆盖上级关系,由于是经过数据核查到的关键数据,所以直接覆盖导入进来的数据即可.
                    Long newParentId = expressionExecutorDetailConfig.getParentId();
                    importInfoConfig.setParentId(newParentId);
                    // 提前获取老的关联编号,目的是为了将导出的数据和导入的环境数据进行关联层级关系
                    importInfoConfig.setId(expressionExecutorDetailConfig.getId());
                    expressionConfigService.updateById(importInfoConfig);
                    idCache.put(oldId, importInfoConfig.getId());
                } else {
                    LOG.debug("trigger insert , executorId : {} , expression code : {} - {}", executorId, importInfoConfig.getExpressionType(), expressionCode);
                    // 这里就是新增的逻辑
                    saveInfoConfig(idCache, importInfoConfig);
                }
                deepUpdateConfigInfo(tree.getChildren(), executorId, dbCodeMap, hitCode, idCache);
            }
        }
    }

    private void deepInfoConfigSave(List<Tree<Long>> treeList, Map<Long, Long> idCache, Long executorId) {
        if (treeList != null) {
            for (Tree<Long> tree : treeList) {
                ExpressionExecutorInfoConfig infoConfig = (ExpressionExecutorInfoConfig) tree.get("obj");
                infoConfig.setExecutorId(executorId);
                saveInfoConfig(idCache, infoConfig);
                deepInfoConfigSave(tree.getChildren(), idCache, executorId);
            }
        }
    }

    /**
     * 多余的数据处理
     *
     * @param codeMap 全量数据
     * @param hitCode 已经处理的数据
     */
    private void excessDataProcessor(Map<String, ExpressionExecutorInfoConfig> codeMap, Set<String> hitCode) {
        // 这里是当前环境多出来的数据处理方式,导入的数据比当前环境少,多出来的这部分数据需要处理.
        Collection<String> deleteCodeList = CollectionUtils.subtract(codeMap.keySet(), hitCode);
        if (!deleteCodeList.isEmpty()) {
            LOG.info("需要删除的deleteCode:{}", deleteCodeList);
            final List<ExpressionExecutorInfoConfig> unValidList = deleteCodeList.stream().map(codeMap::get).peek(var -> {
                var.setExpressionStatus(false);
                final String title = String.format("【导入冲突】-%s", var.getExpressionTitle());
                var.setExpressionTitle(title);
            }).collect(Collectors.toList());
            expressionConfigService.updateBatchById(unValidList);
        }
    }


    private void saveInfoConfig(Map<Long, Long> idCache, ExpressionExecutorInfoConfig infoConfig) {
        Long oldInfoId = infoConfig.getId();
        // 根据之前的上级编号，去缓存中查看是否更新，存储新的级联关系
        Long newParentId = idCache.get(infoConfig.getParentId());
        infoConfig.setParentId(newParentId);
        infoConfig.setId(null);
        expressionConfigService.save(infoConfig);

        Long newInfoId = infoConfig.getId();
        // 当前编号进行关联
        idCache.put(oldInfoId, newInfoId);
    }

    @Override
    public ExpressionExecutorSyncData export(Long id) {
        ExpressionExecutorBaseInfo executorBaseInfo = executorConfigService.getById(id);

        List<ExpressionExecutorInfoConfig> nodeList = expressionConfigService.getExpressionListByBaseId(id);

        ExpressionExecutorSyncData expressionExecutorSyncData = new ExpressionExecutorSyncData();

        expressionExecutorSyncData.setBaseInfo(executorBaseInfo);

        expressionExecutorSyncData.setNodeInfo(nodeList);
        LOG.info("本次导出结果：{} -> {} 条", id, nodeList.size());
        return expressionExecutorSyncData;
    }
}
