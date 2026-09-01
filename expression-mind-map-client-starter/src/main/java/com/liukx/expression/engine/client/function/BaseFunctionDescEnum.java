package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;

import java.util.Arrays;


/**
 * 基础函数定义
 * 基础的函数就以默认的fn开头作为规范
 * <a href="https://www.yuque.com/boyan-avfmj/aviatorscript/ashevw">关于aviator的系统函数</a>
 *
 * @author liukaixiong
 * @date 2023/12/7
 */
public enum BaseFunctionDescEnum implements ExpressFunctionDocumentLoader {
    // @Formatter:off
    END("base_flow_control", "fn_end", "走完该分支流程之后结束", new String[]{}, "true || false", "fn_end()"),
    END_IN("base_flow_control", "fn_in_end", "执行当前分支的内部子分支流程之后结束", new String[]{}, "true || false", "fn_in_end()"),
    END_RETURN("base_flow_control", "fn_return", "返回到上层分支，同级别分支不在继续", new String[]{}, "true || false", "fn_return()"),
    END_IN_RETURN("base_flow_control", "fn_in_return", "执行当前分支的内部子分支流程后,返回到上层分支，同级别分支不在继续", new String[]{}, "true || false", "fn_return()"),
    END_FORCE("base_flow_control", "fn_force_end", "强制终止流程,不在继续执行任何流程", new String[]{}, "true || false", "fn_force_end()"),
    END_ERROR_MESSAGE("base_flow_control", "fn_error_message", "强制终止流程,不在继续执行任何流程,并返回异常结果", new String[]{"错误信息"}, "true || false", "fn_error_message('信息描述')"),
    END_ERROR_EXCEPTION("base_flow_control", "fn_error_exception", "直接抛出异常", new String[]{"错误信息"}, "break", "fn_error_exception('异常信息')"),
    END_REDIRECT("base_flow_control", "fn_redirect", "重定向到指定分支", new String[]{"表达式编码"}, "true || false", "fn_redirect('表达式编码')"),
    DEBUG_BODY("base_debug", "debug_body", "打印请求参数", new String[]{"上下文key"}, "true || false", "debug_body('request')"),
    DEBUG_OBJECT("base_debug", "debug_object", "打印请求参数", new String[]{"上下文对象"}, "true || false", "debug_object(request)"),
    DEBUG_LOG("base_debug", "debug_log", "打印请求参数", new String[]{"打印对象"}, "true || false", "debug_log(param)"),
    DEBUG_TEST_LOG("base_debug", "debug_test_log", "打印请求参数", new String[]{"结果描述","结果值"}, "true || false", "debug_log(param)"),
    ENV_ADD_LIST("base_env", "fn_env_add_list", "添加上下文环境变量", new String[]{"环境变量key", "环境变量值"}, "true || false", "fn_add_env_list('key','value')"),
    ENV_PUT_VALUE("base_env", "fn_env_put_value", "设置上下文环境变量", new String[]{"环境变量key", "环境变量值"}, "true || false", "fn_put_value('key','value')"),
    ENV_PUT_BRANCH_VALUE("base_env", "fn_env_put_branch_value", "设置当前分支的变量，该变量仅存在与该分支内部用作一些关键功能的参数回调", new String[]{"环境变量组或者key", "环境变量值"}, "true || false", "fn_env_put_branch_value('key','value')"),
    ENV_GET_BRANCH_VALUE("base_env", "fn_env_get_branch_value", "获取当前分支的变量", new String[]{"环境变量组或者key"}, "true || false", "fn_env_get_branch_value('key')"),
    ENV_PUT_ALL_VALUE("base_env", "fn_env_put_all_value", "设置上下文环境变量", new String[]{"环境变量key", "环境变量值"}, "true || false", "fn_put_value('key','value')"),
    ENV_GET_VALUE("base_env", "fn_env_get_value", "获取上下文环境变量", new String[]{"环境变量key"}, "true || false", "fn_get_value('key')"),
    ENV_SPRING_GET_VALUE("base_env", "fn_env_spring_get_value", "获取spring中的环境变量", new String[]{"环境变量key","转换的值:obj,list,map","默认值"}, "true || false", "fn_env_spring_get_value('key','value','default')"),
    ENV_SPRING_GET_LIST_VALUE("base_env", "fn_env_spring_get_list", "获取spring中的配置并解析成List集合,第二个参数指定元素类型:string/int/long/double/boolean等。配置支持JSON数组字符串[1,2,3]与逗号分隔1,2,3两种写法,配置不存在返回空集合", new String[]{"环境变量key", "元素类型: 可选,默认string", "默认值: 可选,配置不存在时解析该值"}, "list", "fn_env_spring_get_list('zhiyue-admin.teacher-assign-participating-level-ids', 'long')"),
    ENV_GET_RESULT_MAP_VALUE("base_env", "fn_env_get_result_map", "获取结果缓存中的Map结构的值", new String[]{"组key","键"}, "object", "fn_env_get_result_map('group','key')"),
    ENV_INVOKE_METHOD("base", "fn_env_invoke_method", "执行变量中对应的方法", new String[]{"变量对象", "变量的方法", "变量的参数: 使用seq.list(变量1,变量2,变量3)"}, "true || false", "fn_env_invoke_method(obj,'xxMethod',seq.list(1,2,3))"),
    SYS_SLEEP("base", "fn_sys_sleep", "休眠", new String[]{"毫秒值"}, "true || false", "fn_sys_sleep(5000)"),
    SYS_DATE_HOUR_RANGE("base_sys_date", "fn_sys_date_hour_range", "是否在小时时间范围处理(基于系统时间)", new String[]{"开始小时数", "结束小时数"}, "true || false", "fn_sys_date_hour_range('9','18')"),
    SYS_DATE_DAY_RANGE("base_sys_date", "fn_sys_date_day_range", "是否在日期时间范围处理(基于系统时间)", new String[]{"开始日期", "结束日期"}, "true || false", "fn_sys_date_day_range('2024-08-21','2024-08-25')"),
    SYS_DATE_DAY_TO_LOCAL_DATE("base_sys_date", "fn_sys_date_to_local_date", "将date对象转换成LocalDate对象", new String[]{"Date or 字符串日期"}, "true || false", "fn_sys_date_to_local_date('2024-08-21')"),
    SYS_DATE_DAY_TO_LOCAL_DATE_TIME("base_sys_date", "fn_sys_date_to_local_date_time", "将date对象转换成LocalDateTime对象", new String[]{"Date or 字符串日期"}, "true || false", "fn_sys_date_to_local_date_time('2024-08-21 12:12:12')"),
    RECORD_RESULT_CONTEXT("base_result", "fn_record_result_context", "设置结果到上下文中", new String[]{"键", "值"}, "true || false", "fn_record_result_context('result','abc')"),
    RECORD_RESULT_MAP_CONTEXT("base_result", "fn_record_result_map_context", "设置结果到上下文中", new String[]{"组", "键", "值"}, "true || false", "fn_record_result_map_context('result','abc')"),
    OBJECT_IS_NOT_NULL("base_util", "fn_object_is_not_null", "判断值是否为空,允许传递多个值,请传递变量", new String[]{"值1", "值2"}, "true || false", "fn_object_is_not_null(a1,a2)"),
    OBJECT_STR_TO_JSON("base_util", "fn_str_to_json", "字符串转json对象", new String[]{"json字符串"}, "true || false", "fn_str_to_json(jsonStr)"),
    LIST_STREAM_MAP("base_util", "fn_list_stream_map", "集合转换", new String[]{"集合对象","转换数值"}, "true || false", "fn_list_stream_map(list,lambda(x)-> xxx end)"),
    LIST_FILTER("base_collection", "fn_list_filter", "过滤集合中满足条件的元素,返回新集合", new String[]{"集合对象", "过滤条件: lambda(x)-> 布尔条件 end"}, "list", "fn_list_filter(orderList, lambda(x) -> x.status == 'PAID' end)"),
    LIST_FIRST("base_collection", "fn_list_first", "获取集合中首个满足条件的元素,不存在返回null", new String[]{"集合对象", "过滤条件: lambda"}, "object", "fn_list_first(orderList, lambda(x) -> x.status == 'PAID' end)"),
    LIST_FIRST_FIELD("base_collection", "fn_list_first_field", "获取集合中首个满足过滤条件元素的指定字段值,不存在返回null。取值支持lambda(可计算)或字段名两种形式", new String[]{"集合对象", "过滤条件: lambda", "取值: lambda取值函数或字段名(支持a.b嵌套)"}, "object", "fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, lambda(x) -> x.amount end)"),
    LIST_ANY_MATCH("base_collection", "fn_list_any_match", "判断集合中是否存在满足条件的元素,空集合返回false", new String[]{"集合对象", "过滤条件: lambda"}, "true || false", "fn_list_any_match(orderList, lambda(x) -> x.amount > 100 end)"),
    LIST_ALL_MATCH("base_collection", "fn_list_all_match", "判断集合中是否全部元素都满足条件,空集合返回true", new String[]{"集合对象", "过滤条件: lambda"}, "true || false", "fn_list_all_match(orderList, lambda(x) -> x.status == 'PAID' end)"),
    LIST_COUNT("base_collection", "fn_list_count", "统计集合元素数量,支持传递lambda条件统计满足条件的数量", new String[]{"集合对象", "过滤条件: lambda可选"}, "number", "fn_list_count(orderList, lambda(x) -> x.status == 'PAID' end)"),
    LIST_FIELD_VALUES("base_collection", "fn_list_field_values", "提取集合中所有元素的指定字段值组成新集合", new String[]{"集合对象", "字段名: 支持a.b嵌套"}, "list", "fn_list_field_values(orderList, 'orderId')"),
    LIST_SUM_FIELD("base_collection", "fn_list_sum_field", "对集合中指定数值字段求和,空值会被忽略,空集合返回0", new String[]{"集合对象", "字段名"}, "number", "fn_list_sum_field(orderList, 'amount')"),
    LIST_GROUP("base_collection", "fn_list_group", "按指定字段将集合分组,返回Map结构: {字段值:元素集合}", new String[]{"集合对象", "字段名"}, "map", "fn_list_group(orderList, 'status')"),
    LIST_DISTINCT_FIELD("base_collection", "fn_list_distinct_field", "集合去重,传递字段名按字段值去重,不传按元素本身去重,保留首次出现顺序", new String[]{"集合对象", "字段名: 可选"}, "list", "fn_list_distinct_field(userList, 'userId')"),
    LIST_JOIN_FIELD("base_collection", "fn_list_join_field", "提取指定字段值并用分隔符拼接成字符串,默认逗号分割,空值忽略", new String[]{"集合对象", "字段名", "分隔符: 可选,默认,"}, "string", "fn_list_join_field(orderList, 'orderId', ',')"),
    LIST_SORT_FIELD("base_collection", "fn_list_sort_field", "按指定字段对集合排序返回新集合,默认升序", new String[]{"集合对象", "字段名", "排序方式: 可选asc升序/desc降序"}, "list", "fn_list_sort_field(orderList, 'amount', 'desc')"),
    LIST_CONTAINS("base_collection", "fn_list_contains", "判断集合中是否包含指定值,数值跨精度比较,空集合返回false", new String[]{"集合对象", "判断的值"}, "true || false", "fn_list_contains(userRoles, 'admin')"),
    LIST_MERGE("base_collection", "fn_list_merge", "合并多个集合返回新集合,空集合会被忽略", new String[]{"集合1", "集合2", "集合N"}, "list", "fn_list_merge(blackUsers, grayUsers)"),
    LIST_SIZE("base_collection", "fn_list_size", "获取集合大小,空集合返回0", new String[]{"集合对象"}, "number", "fn_list_size(orderList)"),
    BLACKLIST("base_list", "fn_blacklist", "黑名单操作: 默认查询值是否存在，支持add新增、del删除", new String[]{"名单组", "名单key", "名单值", "操作类型: 可选add/del，默认查询"}, "true || false", "fn_blacklist('risk','ip','192.168.1.1')"),
    WHITELIST("base_list", "fn_whitelist", "白名单操作: 默认查询值是否存在，支持add新增、del删除", new String[]{"名单组", "名单key", "名单值", "操作类型: 可选add/del，默认查询"}, "true || false", "fn_whitelist('trusted','user','admin')"),

    ;
    private final FunctionApiModel functionApiModel;

    BaseFunctionDescEnum(String group, String code, String describe, String[] requestDesc, String returnDescribe, String example) {
        FunctionApiModel def = new FunctionApiModel();
        def.setName(code);
        def.setGroupName(group);
        def.setDescribe(describe);
        def.setResultClassType(returnDescribe);
        def.setArgs(Arrays.asList(requestDesc));
        def.setExample(example);
        this.functionApiModel = def;
    }


    @Override
    public FunctionApiModel loadFunctionInfo() {
        return this.functionApiModel;
    }
}
