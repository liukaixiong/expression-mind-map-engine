package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.server.service.model.function.FunctionInfoDto;

import java.util.List;

/**
 * 函数处理
 * @author liukaixiong
 * @date : 2022/6/10 - 14:01
 */
public interface FunctionService {

    public FunctionInfoDto getKeyInfo(String key);

    public boolean refresh(String serviceName, List<FunctionInfoDto> variableInfoDto);

    List<FunctionInfoDto> loadAllFunctionDefined();

}
