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
