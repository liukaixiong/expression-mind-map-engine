package com.liukx.expression.engine.server.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Splitter;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import com.liukx.expression.engine.core.utils.Jsons;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionDocService;
import com.liukx.expression.engine.server.service.ExpressionExecutorConfigService;
import com.liukx.expression.engine.server.service.ExpressionTraceLogIndexService;
import com.liukx.expression.engine.server.service.ExpressionVarTypeService;
import com.liukx.expression.engine.server.service.model.doc.ExpressionDocDto;
import com.liukx.expression.engine.server.util.MapFlattenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 函数配置表 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Api(tags = "文档管理")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/doc")
@Validated
public class ExpressionDocController {
    @Autowired
    private ExpressionDocService documentService;
    @Autowired
    private ExpressionVarTypeService variableTypeService;
    @Autowired
    private ExpressionExecutorConfigService executorConfigService;
    @Autowired
    private ExpressionTraceLogIndexService traceLogIndexService;

    @ApiOperation("查询信息")
    @GetMapping("/getList")
    public RestResult<List<ExpressionDocDto>> findFuncList(@RequestParam("executorId") Long executorId, @RequestParam(value = "expressionId", required = false) Long expressionId, @RequestParam("name") String name, @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        final ExpressionExecutorBaseInfo executorBaseInfo = executorConfigService.getById(executorId);
        final String serviceName = executorBaseInfo.getServiceName();
        final String varDefinition = executorBaseInfo.getVarDefinition();

        List<ExpressionDocDto> matchExpressionList = new ArrayList<>();

        // 注入追踪变量
        injectTraceEnvList(matchExpressionList, expressionId, name, serviceName);

        if (StringUtils.isNotEmpty(varDefinition) && varDefinition.contains(name)) {
            final Map<String, String> envContext = Splitter.on(',').withKeyValueSeparator("=").split(varDefinition);
            matchExpressionList.addAll(injectEnvNameList(envContext, serviceName, name, "执行器注入变量"));
        }


        List<ExpressionDocDto> expressionList = documentService.getLikeName(executorBaseInfo.getServiceName(), name, limit);
        if (!expressionList.isEmpty()) {
            matchExpressionList.addAll(expressionList);
        }
        return RestResult.ok(matchExpressionList);
    }

    private void injectDocType(Map<String, String> envContext, List<ExpressionDocDto> matchExpressionList, String serviceName, String name) {
        // 戴弃用
        envContext.keySet().stream().filter(key -> key.contains(name)).forEach(var -> {
            final String type = envContext.get(var);
            final List<VariableApiModel> keyInfo = variableTypeService.getKeyInfo(serviceName, type);
            if (keyInfo != null) {
                keyInfo.stream().map(doc -> {
                    ExpressionDocDto docInfo = new ExpressionDocDto();
                    docInfo.setType("var");
                    docInfo.setName(name + "." + doc.getName());
                    docInfo.setGroupName(doc.getGroupName());
                    docInfo.setParams(Collections.singletonList(doc.getType()));
                    docInfo.setServiceName(serviceName);
                    docInfo.setExample(docInfo.getName());
                    docInfo.setDescribe(doc.getDescribe());
                    return docInfo;
                }).forEach(matchExpressionList::add);
            }
        });
        matchExpressionList.addAll(injectEnvNameList(envContext, serviceName, name, "配置变量"));
    }

    private void injectTraceEnvList(List<ExpressionDocDto> matchExpressionList, Long expressionId, String name, String serviceName) {
        if (expressionId != null) {
            final ExpressionTraceLogIndex expressionSampleBody = traceLogIndexService.getExpressionSampleBody(expressionId);
            if (expressionSampleBody != null) {
                final String envBody = expressionSampleBody.getEnvBody();
                Map<String, Object> envContext = Jsons.parseObject(envBody, new TypeReference<>() {
                });

                if (name.indexOf(".") > 0) {
                    envContext = MapFlattenUtil.flatten(envContext);
                }

                Map<String, String> envContextMap = envContext.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));

                matchExpressionList.addAll(injectEnvNameList(envContextMap, serviceName, name, "追踪可用变量"));
            }
        }
    }

    private List<ExpressionDocDto> injectEnvNameList(Map<String, String> envContext, String serviceName, String name, String describe) {
        return envContext.keySet().stream().filter(key -> key.contains(name)).map(var -> {
            final String value = envContext.get(var);
            ExpressionDocDto docInfo = new ExpressionDocDto();
            docInfo.setType("var");
            docInfo.setName(var);
            docInfo.setGroupName(var);
            docInfo.setServiceName(serviceName);
            docInfo.setExample(value);
            docInfo.setDescribe(describe);
            return docInfo;
        }).collect(Collectors.toList());
    }

    @ApiOperation("查询信息")
    @PostMapping("/getTypeList")
    public RestResult<Set<Object>> getTypeList(@RequestParam("executorId") Long executorId) {
        final ExpressionExecutorBaseInfo executorBaseInfo = executorConfigService.getById(executorId);
        final Set<Object> allTypeList = variableTypeService.getAllTypeList(executorBaseInfo.getServiceName());
        return RestResult.ok(allTypeList);
    }

}
