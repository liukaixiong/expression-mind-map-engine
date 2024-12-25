package com.liukx.expression.engine.client.feature;

import com.liukx.expression.engine.client.api.ExpressionExecutorPostProcessor;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 筛选过滤的表达式
 * 开启能力可通过:
 * - ExpressionEnvContext#enableExpressionConfigIdSkipFilter(java.util.Set)
 * - ExpressionEnvContext#enableExpressionConfigIdContainFilter(java.util.Set)
 *
 * @author liukaixiong
 * @date 2024/8/22 - 19:24
 */
@Component
public class ExpressionConfigIdFilterSupport implements ExpressionExecutorPostProcessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void beforeExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo) {

        final Set<Long> expressionConfigIdContainFilter = envContext.getExpressionConfigIdContainFilter();
        if (!CollectionUtils.isEmpty(expressionConfigIdContainFilter)) {
            // 处理指定的表达式数据
            List<ExpressionConfigTreeModel> configTreeModelList = new LinkedList<>();
            processorFilterContainExpressionInfo(configInfo.getConfigTreeModelList(), configTreeModelList, expressionConfigIdContainFilter);
            configInfo.setConfigTreeModelList(configTreeModelList);
            logger.debug("表达式过滤 处理指定的表达式内容:{}", expressionConfigIdContainFilter);
        }

        final Set<Long> expressionConfigIdSkipFilterList = envContext.getExpressionConfigIdSkipFilterList();
        if (!CollectionUtils.isEmpty(expressionConfigIdSkipFilterList)) {
            // 处理需要过滤的表达式数据
            List<ExpressionConfigTreeModel> configTreeModelList = processorFilterSkippedFilter(configInfo.getConfigTreeModelList(), expressionConfigIdSkipFilterList);
            configInfo.setConfigTreeModelList(configTreeModelList);
            logger.debug("表达式过滤 跳过特定的配置项:{}", expressionConfigIdContainFilter);
        }

    }

    private List<ExpressionConfigTreeModel> processorFilterSkippedFilter(List<ExpressionConfigTreeModel> configTreeModelList, Set<Long> expressionConfigIdSkipFilterList) {
        List<ExpressionConfigTreeModel> newConfigTreeModelList = new LinkedList<>();
        if (CollectionUtils.isEmpty(expressionConfigIdSkipFilterList) || CollectionUtils.isEmpty(configTreeModelList)) {
            return newConfigTreeModelList;
        }

        for (ExpressionConfigTreeModel configTreeModel : configTreeModelList) {
            if (!expressionConfigIdSkipFilterList.contains(configTreeModel.getExpressionId())) {
                final List<ExpressionConfigTreeModel> expressionConfigTreeModels = processorFilterSkippedFilter(configTreeModel.getNodeExpression(), expressionConfigIdSkipFilterList);
                configTreeModel.setNodeExpression(expressionConfigTreeModels);
                newConfigTreeModelList.add(configTreeModel);
            }
        }

        return newConfigTreeModelList;
    }


    private void processorFilterContainExpressionInfo(List<ExpressionConfigTreeModel> configTreeModelList, List<ExpressionConfigTreeModel> newConfigTreeModelList, Set<Long> expressionFilterIdList) {
        if (CollectionUtils.isEmpty(configTreeModelList) || expressionFilterIdList.size() == newConfigTreeModelList.size()) {
            return;
        }

        for (ExpressionConfigTreeModel configTreeModel : configTreeModelList) {
            if (expressionFilterIdList.contains(configTreeModel.getExpressionId())) {
                newConfigTreeModelList.add(configTreeModel);
            } else {
                processorFilterContainExpressionInfo(configTreeModel.getNodeExpression(), newConfigTreeModelList, expressionFilterIdList);
            }
        }
    }


    @Override
    public void afterExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configTreeModelList) {

    }

}
