package com.liukx.expression.engine.server.executor;

import com.liukx.expression.engine.client.api.config.RedisExpressionConfigService;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.core.api.model.*;
import com.liukx.expression.engine.core.enums.ExpressionTypeEnum;
import com.liukx.expression.engine.core.utils.Jsons;
import com.liukx.expression.engine.server.components.TraceLogHelper;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.enums.TraceStageEnums;
import com.liukx.expression.engine.server.exception.Throws;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorBaseDTO;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorDetailConfigDTO;
import com.liukx.expression.engine.server.service.ExpressionConfigService;
import com.liukx.expression.engine.server.service.ExpressionExecutorConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 表达式执行器
 * 负责接收上游发送过来的业务编码进行执行逻辑处理
 *
 * @author liukaixiong
 * @date : 2022/6/9 - 14:33
 */
@Component
public class ExpressionExecutor implements ExpressionExecutorService {
    private final Logger log = getLogger(ExpressionExecutor.class);
    private final List<ExpressionTypeEnum> invokeTypeList = Arrays.asList(ExpressionTypeEnum.ACTION, ExpressionTypeEnum.CONDITION, ExpressionTypeEnum.TRIGGER, ExpressionTypeEnum.CALLBACK);
    @Autowired
    private RedisExpressionConfigService redisExpressionConfigService;
    @Autowired
    private ExpressionExecutorFactory executorFactory;
    @Autowired
    private ExpressionExecutorConfigService expressionExecutorConfigService;
    @Autowired
    private ExpressionConfigService expressionConfigService;

    public ExpressionConfigInfo getBusinessConfigInfo(String serviceName, String businessCode, String executorCode) {

        // 1. 根据项目和业务编码查询配置的表达式配置组
        ExpressionExecutorBaseDTO expressionExecutorBase = expressionExecutorConfigService.queryExecutorInfo(serviceName, businessCode, executorCode);

        Throws.check(expressionExecutorBase == null, "未查询到执行器[" + serviceName + "|" + businessCode + "|" + executorCode + "]配置信息!");

        List<ExpressionConfigTreeModel> deepConfigInfo = getDeepConfigInfo(expressionExecutorBase.getId());

        Map<String, Object> configurabilityMap = new HashMap<>();

        if (StringUtils.isNotEmpty(expressionExecutorBase.getConfigurabilityJson())) {
            configurabilityMap = Jsons.parseMap(expressionExecutorBase.getConfigurabilityJson());
        }

        return ExpressionConfigInfo.builder().executorId(expressionExecutorBase.getId()).configurabilityMap(configurabilityMap).executorCode(expressionExecutorBase.getExecutorCode()).executorName(expressionExecutorBase.getExecutorDescription()).serviceName(serviceName).businessCode(businessCode).configTreeModelList(deepConfigInfo).build();
    }


    public ExpressionConfigInfo queryBusinessConfigInfo(String serviceName, String businessCode, String executorCode) {
        Object configInfoObject = redisExpressionConfigService.getConfigInfo(serviceName, businessCode, executorCode);
        if (configInfoObject != null) {
            return (ExpressionConfigInfo) configInfoObject;
        }
        return getRefreshBusinessConfigInfo(serviceName, businessCode, executorCode);
    }

    public ExpressionConfigInfo getRefreshBusinessConfigInfo(String serviceName, String businessCode, String executorCode) {
        ExpressionConfigInfo businessConfigInfo = getBusinessConfigInfo(serviceName, businessCode, executorCode);
        redisExpressionConfigService.refreshConfigInfo(serviceName, businessCode, executorCode, businessConfigInfo);
        return businessConfigInfo;
    }

    /**
     * 获取表达式配置表中的父子关系集合
     *
     * @param executorId 执行器编号
     * @return
     */
    private List<ExpressionConfigTreeModel> getDeepConfigInfo(Long executorId) {
        return getDeepConfigInfo(executorId, 0L);
    }

    /**
     * 递归获取数据库中相应的表达式层级目录结构
     *
     * @param executorId 上级编号
     * @param parentId   上级编号
     * @return
     */
    private List<ExpressionConfigTreeModel> getDeepConfigInfo(Long executorId, Long parentId) {
        // 获取该编号的下级表达式集合
        List<ExpressionExecutorDetailConfigDTO> nodeExpressionInfo = expressionConfigService.getNodeExpressionInfo(executorId, parentId);

        if (CollectionUtils.isEmpty(nodeExpressionInfo)) {
            return null;
        }

        List<ExpressionConfigTreeModel> deepList = new ArrayList<>();
        for (ExpressionExecutorDetailConfigDTO configDTO : nodeExpressionInfo) {
            // 启用的表达式才会应用deepList = {ArrayList@17114}  size = 2
            if (BaseConstants.BASE_VALID_STATUS == configDTO.getExpressionStatus()) {
                // 根据当前表达式配置,获取下级表达式内容
                List<ExpressionConfigTreeModel> deepConfigInfo = getDeepConfigInfo(configDTO.getExecutorId(), configDTO.getId());
                ExpressionConfigTreeModel configTreeModel = ExpressionConfigTreeModel.builder()
                        .executorId(configDTO.getExecutorId())
                        .expressionCode(configDTO.getExpressionCode())
                        .expressionId(configDTO.getId())
                        .expressionType(configDTO.getExpressionType())
                        .expression(configDTO.getExpressionContent())
                        .title(configDTO.getExpressionTitle())
                        .nodeExpression(deepConfigInfo).build();
                deepList.add(configTreeModel);
            }
        }

        return deepList;
    }


    /**
     * 表达式触发
     *
     * @param serviceName
     * @param businessCode
     * @param request
     */
    @Override
    public Map<String, Object> invoke(String serviceName, String businessCode, String eventName, String unionId, String traceId, Object request) {
        return null;
    }

    private void executorExpression(ExpressionBaseRequest baseRequest, String eventName, Long executorBaseId, Long parentId, int index) {

        if (invokeTypeList.size() <= index) {
            TraceLogHelper.recordLog(baseRequest, TraceStageEnums.ERROR, "执行完成！");
            return;
        }

        ExpressionService expressionService = executorFactory.getExpressionService();

        ExpressionTypeEnum expressionTypeEnum = invokeTypeList.get(index);

        List<ExpressionExecutorDetailConfigDTO> ruleInfo = expressionConfigService.getEventInfo(executorBaseId, parentId, eventName, expressionTypeEnum);

        if (CollectionUtils.isEmpty(ruleInfo)) {
            TraceLogHelper.recordLog(baseRequest, TraceStageEnums.valueOf(expressionTypeEnum.name()), String.format("没有找到对应的【%s -> %s】规则！", executorBaseId, expressionTypeEnum.name()));
            return;
        }

        for (ExpressionExecutorDetailConfigDTO configDTO : ruleInfo) {

            String title = configDTO.getExpressionTitle();

            Long baseId = configDTO.getId();

            String expressionType = configDTO.getExpressionType();

            String expressionContent = configDTO.getExpressionContent();

            TraceLogHelper.recordLog(baseRequest, TraceStageEnums.valueOf(expressionType.toUpperCase()), String.format("开始匹配: %s,%s ！", title, expressionContent));


            Map<String, Object> env = new HashMap<>();
            env.put("req", baseRequest);
            Object execute = expressionService.execute(expressionContent, env);

            if (execute instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) execute;
                //baseRequest.setResultMap(map);
            }

            if (execute instanceof Boolean) {
                if (!((Boolean) execute)) {
                    TraceLogHelper.recordLog(baseRequest, TraceStageEnums.ERROR, String.format("执行结果-> 标题：%s 内容：%s , 結果：%s , 未通过!", title, expressionContent, execute));
                    continue;
                }
            }

            TraceLogHelper.recordLog(baseRequest, TraceStageEnums.END_MATCH, String.format("匹配值: %s = %s ！", title, execute));

            executorExpression(baseRequest, null, executorBaseId, baseId, index + 1);
        }
    }

    private ExpressionBaseRequest convertBaseRequest(String serviceName, String businessCode, String eventName, String unionId, String traceId, Object request) {
        ExpressionBaseRequest baseRequest = new ExpressionBaseRequest();
        baseRequest.setServiceName(serviceName);
        baseRequest.setBusinessCode(businessCode);
        baseRequest.setEventName(eventName);
        baseRequest.setUnionId(unionId);
        baseRequest.setTraceId(StringUtils.defaultString(traceId, UUID.randomUUID().toString()));
        if (request != null) {
            //baseRequest.setParamsMap(Convert.toMap(String.class, Object.class, request));
        }
        return baseRequest;
    }

}
