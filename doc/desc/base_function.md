### 📂 基础流程控制函数 (base_flow_control)

| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `fn_end()` | 走完该分支流程之后结束 | 无 | `fn_end()` |
| `fn_in_end()` | 执行当前分支的内部子分支流程之后结束 | 无 | `fn_in_end()` |
| `fn_return()` | 返回到上层分支，同级别分支不再继续 | 无 | `fn_return()` |
| `fn_in_return()` | 执行当前分支的内部子分支流程后,返回到上层分支，同级别分支不在继续 | 无 | `fn_in_return()` |
| `fn_force_end()` | 强制终止流程,不再继续执行任何流程 | 无 | `fn_force_end()` |
| `fn_error_message(info)` | 强制终止流程，并返回异常信息 | `info`: 错误信息 | `fn_error_message('信息描述')` |
| `fn_error_exception(info)` | 直接抛出异常 | `info`: 错误信息 | `fn_error_exception('异常信息')` |
| `fn_redirect(exprKey)` | 重定向到指定分支 | `exprKey`: 表达式编码 | `fn_redirect('expr_001')` |

### 🐞 基础调试函数 (base_debug)

| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `debug_body(key)` | 打印请求参数 | `key`: 上下文key | `debug_body('request')` |
| `debug_object(obj)` | 打印请求参数 | `obj`: 上下文对象 | `debug_object(request)` |
| `debug_log(param)` | 打印请求参数 | `param`: 打印对象 | `debug_log(param)` |
| `debug_test_log(desc, value)` | 打印测试日志 | `desc`: 结果描述<br>`value`: 结果值 | `debug_test_log('result', param)` |

### 🌍 基础环境变量函数 (base_env)

| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `fn_env_add_list(key, value)` | 添加上下文环境变量 | `key`: 环境变量key<br>`value`: 环境变量值 | `fn_add_env_list('key','value')` |
| `fn_env_put_value(key, value)` | 设置上下文环境变量 | `key`: 环境变量key<br>`value`: 环境变量值 | `fn_put_value('key','value')` |
| `fn_env_put_branch_value(groupOrKey, value)` | 设置当前分支的变量 (仅内部回调) | `groupOrKey`: 环境变量组或key<br>`value`: 环境变量值 | `fn_env_put_branch_value('key','value')` |
| `fn_env_get_branch_value(groupOrKey)` | 获取当前分支的变量 | `groupOrKey`: 环境变量组或key | `fn_env_get_branch_value('key')` |
| `fn_env_put_all_value(key, value)` | 设置上下文环境变量 | `key`: 环境变量key<br>`value`: 环境变量值 | `fn_put_value('key','value')` |
| `fn_env_get_value(key)` | 获取上下文环境变量 | `key`: 环境变量key | `fn_get_value('key')` |
| `fn_env_spring_get_value(key, type, default)` | 获取 Spring 中的环境变量 | `key`: 环境变量key<br>`type`: 转换的值类型 (obj, list, map)<br>`default`: 默认值 | `fn_env_spring_get_value('key','map','{}')` |
| `fn_env_spring_get_list(key, elementType, default)` | 获取 Spring 配置并解析成指定元素类型的 List | `key`: 环境变量key<br>`elementType`: 元素类型 (string/int/long/double/boolean等, 默认string)<br>`default`: 默认值 (可选) | `fn_env_spring_get_list('zhiyue-admin.teacher-assign-participating-level-ids', 'long')` |
| `fn_env_get_result_map(group, key)` | 获取结果缓存中的 Map 结构的值 | `group`: 组key<br>`key`: 键 | `fn_env_get_result_map('group','key')` |
| `fn_env_invoke_method(obj, method, params)` | 执行变量中对应的方法 | `obj`: 变量对象<br>`method`: 变量的方法<br>`params`: 变量的参数 (需用 seq.list 包装) | `fn_env_invoke_method(obj,'xxMethod',seq.list(1,2,3))` |

### ⏱️ 基础系统与时间函数 (base_sys_date)

| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `fn_sys_sleep(ms)` | 休眠 | `ms`: 毫秒值 | `fn_sys_sleep(5000)` |
| `fn_sys_date_hour_range(start, end)` | 判断当前时间是否在小时范围内 | `start`: 开始小时数<br>`end`: 结束小时数 | `fn_sys_date_hour_range('9','18')` |
| `fn_sys_date_day_range(start, end)` | 判断当前时间是否在日期范围内 | `start`: 开始日期<br>`end`: 结束日期 | `fn_sys_date_day_range('2024-08-21','2024-08-25')` |
| `fn_sys_date_to_local_date(date)` | 将 Date 对象转换成 LocalDate 对象 | `date`: Date 或 字符串日期 | `fn_sys_date_to_local_date('2024-08-21')` |
| `fn_sys_date_to_local_date_time(date)` | 将 Date 对象转换成 LocalDateTime 对象 | `date`: Date 或 字符串日期 | `fn_sys_date_to_local_date_time('2024-08-21 12:12:12')` |

### 📥 结果记录与工具函数 (base_util / base_result)

| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `fn_record_result_context(key, value)` | 设置结果到上下文中 | `key`: 键<br>`value`: 值 | `fn_record_result_context('result','abc')` |
| `fn_record_result_map_context(group, key, value)` | 设置结果到上下文中 (Map结构) | `group`: 组<br>`key`: 键<br>`value`: 值 | `fn_record_result_map_context('result','key','abc')` |
| `fn_object_is_not_null(val1, val2...)` | 判断值是否为空 (允许传递多个值) | `val1`, `val2...`: 待检测的值 | `fn_object_is_not_null(a1,a2)` |
| `fn_str_to_json(str)` | 字符串转 JSON 对象 | `str`: json字符串 | `fn_str_to_json(jsonStr)` |
| `fn_list_stream_map(list, lambda)` | 集合转换 | `list`: 集合对象<br>`lambda`: 转换数值 (Lambda表达式) | `fn_list_stream_map(list,lambda(x)-> x*2 end)` |

### 📚 集合函数 (base_collection)

所有集合函数对空集合/null 安全。过滤条件使用 lambda 形式：`lambda(x) -> 布尔条件 end`；字段名支持 `a.b` 嵌套取值 (如 `user.name`)。

| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `fn_list_filter(list, lambda)` | 过滤集合中满足条件的元素,返回新集合 | `list`: 集合对象<br>`lambda`: 过滤条件 | `fn_list_filter(orderList, lambda(x) -> x.status == 'PAID' end)` |
| `fn_list_first(list, lambda)` | 获取首个满足条件的元素,不存在返回null | `list`: 集合对象<br>`lambda`: 过滤条件 | `fn_list_first(orderList, lambda(x) -> x.status == 'PAID' end)` |
| `fn_list_first_field(list, lambda, field)` | 获取首个满足过滤条件元素的指定字段值,不存在返回null。第三参数支持lambda取值(可计算)或字段名 | `list`: 集合对象<br>`lambda`: 过滤条件<br>`field`: 取值lambda或字段名(支持a.b嵌套) | `fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, lambda(x) -> x.amount end)` |
| `fn_list_any_match(list, lambda)` | 是否存在满足条件的元素,空集合返回false | `list`: 集合对象<br>`lambda`: 过滤条件 | `fn_list_any_match(orderList, lambda(x) -> x.amount > 100 end)` |
| `fn_list_all_match(list, lambda)` | 是否全部元素满足条件,空集合返回true | `list`: 集合对象<br>`lambda`: 过滤条件 | `fn_list_all_match(orderList, lambda(x) -> x.status == 'PAID' end)` |
| `fn_list_count(list, lambda?)` | 统计元素数量,可选条件统计 | `list`: 集合对象<br>`lambda`: 过滤条件 (可选) | `fn_list_count(orderList, lambda(x) -> x.status == 'PAID' end)` |
| `fn_list_field_values(list, field)` | 提取所有元素的指定字段值组成新集合 | `list`: 集合对象<br>`field`: 字段名 | `fn_list_field_values(orderList, 'orderId')` |
| `fn_list_sum_field(list, field)` | 对指定数值字段求和,空值忽略,空集合返回0 | `list`: 集合对象<br>`field`: 字段名 | `fn_list_sum_field(orderList, 'amount')` |
| `fn_list_group(list, field)` | 按字段分组,返回Map: {字段值:元素集合} | `list`: 集合对象<br>`field`: 字段名 | `fn_list_group(orderList, 'status')` |
| `fn_list_distinct_field(list, field?)` | 去重,传字段按字段值去重,不传按元素去重 | `list`: 集合对象<br>`field`: 字段名 (可选) | `fn_list_distinct_field(userList, 'userId')` |
| `fn_list_join_field(list, field, sep?)` | 提取字段值拼接成字符串,默认逗号,空值忽略 | `list`: 集合对象<br>`field`: 字段名<br>`sep`: 分隔符 (可选,默认`,`) | `fn_list_join_field(orderList, 'orderId', ',')` |
| `fn_list_sort_field(list, field, order?)` | 按字段排序返回新集合,默认升序 | `list`: 集合对象<br>`field`: 字段名<br>`order`: asc/desc (可选) | `fn_list_sort_field(orderList, 'amount', 'desc')` |
| `fn_list_contains(list, value)` | 是否包含指定值,数值跨精度比较 | `list`: 集合对象<br>`value`: 判断的值 | `fn_list_contains(userRoles, 'admin')` |
| `fn_list_merge(list1, list2, ...)` | 合并多个集合返回新集合 | `list1`, `list2`...: 集合对象 | `fn_list_merge(blackUsers, grayUsers)` |
| `fn_list_size(list)` | 获取集合大小,空集合返回0 | `list`: 集合对象 | `fn_list_size(orderList)` |
