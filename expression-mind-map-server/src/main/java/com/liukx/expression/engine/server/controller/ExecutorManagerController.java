package com.liukx.expression.engine.server.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.event.ExecutorConfigRefreshEvent;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorBaseDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionExecutorConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-12
 */
//@Api(tags = "执行器管理")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/executor/info")
public class ExecutorManagerController {
    @Autowired
    private ExpressionExecutorConfigService executorConfigService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    //    @ApiOperation("添加单个执行器")
    @PostMapping("/addOne")
    public RestResult<ExpressionExecutorBaseDTO> addOne(@RequestBody AddExpressionExecutorRequest addExpressionExecutorRequest) {
        return executorConfigService.addExpressionExecutor(addExpressionExecutorRequest);
    }

    //    @ApiOperation("编辑单个执行器")
    @PostMapping("/editOne")
    public RestResult<ExpressionExecutorBaseDTO> editOne(@RequestBody EditExpressionExecutorRequest editRequest) {
        return executorConfigService.editExpressionExecutor(editRequest);
    }

    //    @ApiOperation("查询执行器")
    @PostMapping("/findExecutorList")
    public RestResult<Page<ExpressionExecutorBaseInfo>> findExecutorList(@RequestBody QueryExpressionExecutorRequest queryRequest) {
        final Page<ExpressionExecutorBaseInfo> expressionExecutorBaseInfoPage = executorConfigService.queryExpressionExecutor(queryRequest);
        return RestResult.ok(expressionExecutorBaseInfoPage);
    }

    //    @ApiOperation("查询单个执行器")
    @PostMapping("/findExecutorInfo")
    public RestResult<ExpressionExecutorBaseDTO> findExecutorList(@RequestParam("id") Long id) {
        final ExpressionExecutorBaseInfo info = executorConfigService.getById(id);
        ExpressionExecutorBaseDTO dto = new ExpressionExecutorBaseDTO();
        BeanUtil.copyProperties(info, dto);
        return RestResult.ok(dto);
    }

    //    @ApiOperation("批量逻辑删除执行器")
    @PostMapping("/batchDelete")
    public RestResult<?> batchDelete(@RequestBody @Validated DeleteByIdListRequest delRequest) {
        return executorConfigService.batchDeleteByIdList(delRequest);
    }

    //    @ApiOperation("刷新执行器相关数据")
    @GetMapping("/refreshOne")
    public RestResult<?> refreshOne(@RequestParam("id") Long executorId) {
        eventPublisher.publishEvent(new ExecutorConfigRefreshEvent(executorId));
        return RestResult.ok();
    }


}
