package com.liukx.expression.engine.server.util;

import cn.hutool.core.convert.Convert;
import com.liukx.expression.engine.core.api.model.FunctionRequestDocumentModel;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import com.liukx.expression.engine.core.utils.Jsons;
import com.liukx.expression.engine.server.mapper.entity.ExpressionFunctionConfig;
import com.liukx.expression.engine.server.mapper.entity.ExpressionVariableConfig;
import com.liukx.expression.engine.server.service.model.function.FunctionInfoDto;
import com.liukx.expression.engine.server.service.model.variable.VariableInfoDto;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义内部对象转换
 *
 * @author liukaixiong
 * @date : 2022/6/15 - 17:55
 */
public class ConvertObjectUtils {
    /**
     * 将集合泛型进行转换
     *
     * @param list
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> convertList(List<?> list, Class<T> clazz) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream().map(var -> Convert.convert(clazz, var)).collect(Collectors.toList());
    }

    public static VariableInfoDto convertVariableInfo(String serviceName, VariableApiModel apiModel) {
        VariableInfoDto variableInfoDto = VariableInfoDto.builder()
                .type(apiModel.getType()).serviceName(serviceName)
                .registerType(apiModel.getRegisterType())
                .describe(apiModel.getDescribe())
                .name(apiModel.getName()).build();
        return variableInfoDto;
    }

    public static VariableInfoDto convertVariableInfoDto(ExpressionVariableConfig config) {
        VariableInfoDto infoDto = VariableInfoDto.builder()
                .serviceName(config.getServiceName())
                .name(config.getVarCode())
                .describe(config.getVarDescription())
                .type(config.getVarDataType())
                .registerType(config.getVarSource())
                .build();
        return infoDto;
    }

    public static VariableApiModel convertVariableApiInfo(VariableInfoDto infoDto) {
        VariableApiModel apiModel = new VariableApiModel();
        apiModel.setName(infoDto.getName());
        apiModel.setDescribe(infoDto.getDescribe());
        apiModel.setType(infoDto.getType());
        apiModel.setRegisterType("remote");
        apiModel.setGroupName(infoDto.getServiceName());
        return apiModel;
    }

    public static FunctionInfoDto convertFunctionInfoDto(ExpressionFunctionConfig config) {
        FunctionInfoDto dto = FunctionInfoDto.builder()
                .serviceName(config.getServiceName())
                .name(config.getFuncName())
                .describe(config.getFuncDescription())
                .resultClassType(config.getParamDoc())
                //  .registerType(config.get)
                .build();

        String bodyParam = config.getParamDoc();
        if (bodyParam != null) {
            List<FunctionRequestDocumentModel> documentList = Jsons.parseList(bodyParam, FunctionRequestDocumentModel.class);
            dto.setDocumentModel(documentList);
        }

        return dto;
    }

    public static VariableApiModel convertVariableApiModel(VariableInfoDto dto) {
        VariableApiModel api = Convert.convert(VariableApiModel.class, dto);
        api.setGroupName(dto.getServiceName());
        return api;
    }

    public static FunctionApiModel convertFunctionApiModel(FunctionInfoDto dto) {
        FunctionApiModel convert = Convert.convert(FunctionApiModel.class, dto);
        return convert;
    }


}
