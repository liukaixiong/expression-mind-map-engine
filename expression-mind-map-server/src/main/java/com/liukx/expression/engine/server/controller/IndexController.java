package com.liukx.expression.engine.server.controller;

import com.liukx.expression.engine.core.model.ExpressionUser;
import com.liukx.expression.engine.jdk.utils.ServletUtil;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.model.dto.request.LoginModel;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.IExpressionLoginService;
import com.liukx.expression.engine.server.service.IExpressionTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 远端节点提交执行器
 */
@Api(tags = "首页")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/")
public class IndexController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IExpressionLoginService loginService;

    @Autowired
    private IExpressionTokenService tokenService;

    @ApiOperation("登录接口")
    @PostMapping("/login")
    public RestResult<Object> executor(@RequestBody LoginModel loginModel) {
        ExpressionUser user = loginService.authenticate(loginModel);
        logger.info("username :{}, result:{}", loginModel.getUsername(), user != null);
        if (user != null) {
            final String token = tokenService.tokenCreated(loginModel);

            Map<String, Object> tokenMap = new HashMap<>();
            int timeOut = 1;
            tokenMap.put("tokenName", BaseConstants.TOKEN_NAME);
            tokenMap.put("tokenValue", token);
            tokenMap.put("tokenTimeoutDay", timeOut);
            tokenMap.put("userId", user.getUserId());
            tokenMap.put("username", user.getUsername());
            ServletUtil.setCookie(BaseConstants.TOKEN_NAME, token, (int) TimeUnit.SECONDS.toDays(timeOut));
            return RestResult.ok(tokenMap);
        }
        return RestResult.failed("验证失败");
    }

}
