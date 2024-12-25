package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.server.service.model.variable.VariableInfoDto;

import java.util.List;

/**
 * 变量管理
 * @author liukaixiong
 * @date : 2022/6/10 - 12:00
 */
public interface VariableService {

    /**
     * 获取变量信息
     * @param key
     * @return
     */
    public VariableInfoDto getKeyInfo(String key);

    public boolean refresh(String serviceName, List<VariableInfoDto> variableInfoDto);

    /**
     * 加载所有变量
     * @return
     */
    List<VariableInfoDto> loadAllVariableDefined();
}
