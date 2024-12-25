package com.liukx.expression.engine.server.controller;


import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionNodeDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionKeyService;
import com.liukx.expression.engine.server.service.ExpressionNodeConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 引擎节点信息 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Api(tags = "节点管理")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/node")
@Slf4j
public class NodeConfigController {

    @Autowired
    private ExpressionNodeConfigService nodeConfigService;

    @Autowired
    private ExpressionKeyService keyService;

    @ApiOperation("添加单个服务节点")
    @PostMapping("/addOne")
    public RestResult<ExpressionNodeDTO> addOne(@Validated @RequestBody AddExpressionNodeRequest addExpressionNodeRequest) {
        return nodeConfigService.addExpressionNode(addExpressionNodeRequest);
    }

    @ApiOperation("编辑单个服务节点")
    @PostMapping("/editOne")
    public RestResult<ExpressionNodeDTO> editOne(@RequestBody @Validated EditExpressionNodeRequest editRequest) {
        return nodeConfigService.editExpressionNode(editRequest);
    }

    @ApiOperation("查询服务节点")
    @PostMapping("/findNode")
    public RestResult<List<ExpressionNodeDTO>> findNodeList(@RequestBody QueryExpressionNodeRequest queryRequest) {
        return nodeConfigService.queryExpressionNode(queryRequest);
    }

    @ApiOperation("批量逻辑删除服务节点")
    @PostMapping("/batchDelete")
    public RestResult<?> batchDelete(@RequestBody @Validated DeleteByIdListRequest delRequest) {
        return nodeConfigService.batchDeleteByIdList(delRequest);
    }

//    @ApiOperation("节点数据刷新")
//    @GetMapping("/refreshKey")
//    public RestResult<?> refresh(@RequestParam(name = "serviceName") String serviceName) {
//        try {
//            boolean result = keyService.refreshDocument(serviceName);
//            log.debug("更新节点关键字:{} -> {}", serviceName, result);
//            return RestResult.ok();
//        } catch (Exception e) {
//            return RestResult.failed("节点数据更新失败:" + e.getMessage());
//        }
//
//    }
}
