package com.liukx.expression.engine.server.controller;

import com.liukx.expression.engine.server.components.html.DefaultDataRender;
import com.liukx.expression.engine.server.components.html.HtmlComponentHelper;
import com.liukx.expression.engine.server.enums.RemoteInvokeTypeEnums;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 远端节点提交执行器
 */
@Api(tags = "远端节点提交执行器")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ApiController implements InitializingBean {

    private Map<String, List<DefaultDataRender>> dataMap = new HashMap<>();

    @ApiOperation("后端枚举常量")
    @GetMapping("/enumList")
    public RestResult<Object> executor(
            @RequestParam("code")
            String code) {
        List<DefaultDataRender> defaultDataRenders = dataMap.getOrDefault(code, new ArrayList<>());
        return RestResult.ok(defaultDataRenders);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dataMap.put("remoteInvokeType", HtmlComponentHelper.convertDataRender(RemoteInvokeTypeEnums.class));
    }
}
