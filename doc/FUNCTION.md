# 表达式引擎内置函数与变量

本文档列举了表达式引擎中所有内置的函数和变量定义。

---

## 内置函数 (BaseFunctionDescEnum)

所有基础函数以 `fn` 开头命名。

### 流程控制 (base_flow_control)

| 函数名 | 描述 | 参数 | 返回值 | 示例 |
|--------|------|------|--------|------|
| `fn_end` | 走完该分支流程之后结束 | 无 | `true \|\| false` | `fn_end()` |
| `fn_in_end` | 执行当前分支的内部子分支流程之后结束 | 无 | `true \|\| false` | `fn_in_end()` |
| `fn_return` | 返回到上层分支，同级别分支不在继续 | 无 | `true \|\| false` | `fn_return()` |
| `fn_in_return` | 执行当前分支的内部子分支流程后，返回到上层分支，同级别分支不在继续 | 无 | `true \|\| false` | `fn_return()` |
| `fn_force_end` | 强制终止流程，不在继续执行任何流程 | 无 | `true \|\| false` | `fn_force_end()` |
| `fn_error_message` | 强制终止流程，不在继续执行任何流程，并返回异常结果 | 错误信息 | `true \|\| false` | `fn_error_message('信息描述')` |
| `fn_error_exception` | 直接抛出异常 | 错误信息 | `break` | `fn_error_exception('异常信息')` |
| `fn_redirect` | 重定向到指定分支 | 表达式编码 | `true \|\| false` | `fn_redirect('表达式编码')` |

### 调试 (base_debug)

| 函数名 | 描述 | 参数 | 返回值 | 示例 |
|--------|------|------|--------|------|
| `debug_body` | 打印请求参数 | 上下文key | `true \|\| false` | `debug_body('request')` |
| `debug_object` | 打印请求参数 | 上下文对象 | `true \|\| false` | `debug_object(request)` |
| `debug_log` | 打印请求参数 | 打印对象 | `true \|\| false` | `debug_log(param)` |

### 环境变量 (base_env)

| 函数名 | 描述 | 参数 | 返回值 | 示例 |
|--------|------|------|--------|------|
| `fn_env_add_list` | 添加上下文环境变量 | 环境变量key，环境变量值 | `true \|\| false` | `fn_add_env_list('key','value')` |
| `fn_env_put_value` | 设置上下文环境变量 | 环境变量key，环境变量值 | `true \|\| false` | `fn_put_value('key','value')` |
| `fn_env_put_branch_value` | 设置当前分支的变量，该变量仅存在与该分支内部用作一些关键功能的参数回调 | 环境变量组或者key，环境变量值 | `true \|\| false` | `fn_env_put_branch_value('key','value')` |
| `fn_env_get_branch_value` | 获取当前分支的变量 | 环境变量组或者key | `true \|\| false` | `fn_env_get_branch_value('key')` |
| `fn_env_put_all_value` | 设置上下文环境变量 | 环境变量key，环境变量值 | `true \|\| false` | `fn_put_value('key','value')` |
| `fn_env_get_value` | 获取上下文环境变量 | 环境变量key | `true \|\| false` | `fn_get_value('key')` |
| `fn_env_get_result_map` | 获取结果缓存中的Map结构的值 | 组key，键 | `object` | `fn_env_get_result_map('group','key')` |
| `fn_env_invoke_method` | 执行变量中对应的方法 | 变量对象，变量的方法，变量的参数: 使用seq.list(变量1,变量2,变量3) | `true \|\| false` | `fn_env_invoke_method(obj,'xxMethod',seq.list(1,2,3))` |
| `fn_env_spring_get_list` | 获取spring配置并解析成List,第二个参数指定元素类型(string/int/long/double/boolean等),支持JSON数组字符串与逗号分隔两种配置,配置不存在返回空集合 | 环境变量key，元素类型(可选,默认string)，默认值(可选) | `list` | `fn_env_spring_get_list('zhiyue-admin.teacher-assign-participating-level-ids', 'long')` |

### 系统函数 (base / base_sys_date)

| 函数名 | 描述 | 参数 | 返回值 | 示例 |
|--------|------|------|--------|------|
| `fn_sys_sleep` | 休眠 | 毫秒值 | `true \|\| false` | `fn_sys_sleep(5000)` |
| `fn_sys_date_hour_range` | 是否在小时时间范围处理(基于系统时间) | 开始小时数，结束小时数 | `true \|\| false` | `fn_sys_date_hour_range('9','18')` |
| `fn_sys_date_day_range` | 是否在日期时间范围处理(基于系统时间) | 开始日期，结束日期 | `true \|\| false` | `fn_sys_date_day_range('2024-08-21','2024-08-25')` |
| `fn_sys_date_to_local_date` | 将date对象转换成LocalDate对象 | Date or 字符串日期 | `true \|\| false` | `fn_sys_date_to_local_date('2024-08-21')` |
| `fn_sys_date_to_local_date_time` | 将date对象转换成LocalDateTime对象 | Date or 字符串日期 | `true \|\| false` | `fn_sys_date_to_local_date_time('2024-08-21 12:12:12')` |

### 结果处理 (base_result)

| 函数名 | 描述 | 参数 | 返回值 | 示例 |
|--------|------|------|--------|------|
| `fn_record_result_context` | 设置结果到上下文中 | 键，值 | `true \|\| false` | `fn_record_result_context('result','abc')` |
| `fn_record_result_map_context` | 设置结果到上下文中 | 组，键，值 | `true \|\| false` | `fn_record_result_map_context('result','abc')` |

### 工具函数 (base_util)

| 函数名 | 描述 | 参数 | 返回值 | 示例 |
|--------|------|------|--------|------|
| `fn_object_is_not_null` | 判断值是否为空，允许传递多个值,请传递变量 | 值1，值2 | `true \|\| false` | `fn_object_is_not_null(a1,a2)` |
| `fn_str_to_json` | 字符串转json对象 | json字符串 | `true \|\| false` | `fn_str_to_json(jsonStr)` |

### 集合函数 (base_collection)

所有集合函数对空集合/null 安全。过滤条件使用 lambda 形式：`lambda(x) -> 布尔条件 end`，与 `fn_list_stream_map` 一致；字段名支持 `a.b` 嵌套取值。

| 函数名 | 描述 | 参数 | 返回值 | 示例 |
|--------|------|------|--------|------|
| `fn_list_filter` | 过滤集合中满足条件的元素,返回新集合 | 集合对象，过滤条件lambda | `list` | `fn_list_filter(orderList, lambda(x) -> x.status == 'PAID' end)` |
| `fn_list_first` | 获取集合中首个满足条件的元素,不存在返回null | 集合对象，过滤条件lambda | `object` | `fn_list_first(orderList, lambda(x) -> x.status == 'PAID' end)` |
| `fn_list_first_field` | 获取集合中首个满足过滤条件元素的指定字段值,不存在返回null。取值支持lambda(可计算)或字段名 | 集合对象，过滤条件lambda，取值lambda或字段名 | `object` | `fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, lambda(x) -> x.amount end)` |
| `fn_list_any_match` | 判断集合中是否存在满足条件的元素,空集合返回false | 集合对象，过滤条件lambda | `true \|\| false` | `fn_list_any_match(orderList, lambda(x) -> x.amount > 100 end)` |
| `fn_list_all_match` | 判断集合中是否全部元素都满足条件,空集合返回true | 集合对象，过滤条件lambda | `true \|\| false` | `fn_list_all_match(orderList, lambda(x) -> x.status == 'PAID' end)` |
| `fn_list_count` | 统计集合元素数量,支持传递lambda条件统计满足条件的数量 | 集合对象，过滤条件lambda(可选) | `number` | `fn_list_count(orderList, lambda(x) -> x.status == 'PAID' end)` |
| `fn_list_field_values` | 提取集合中所有元素的指定字段值组成新集合 | 集合对象，字段名 | `list` | `fn_list_field_values(orderList, 'orderId')` |
| `fn_list_sum_field` | 对集合中指定数值字段求和,空值会被忽略,空集合返回0 | 集合对象，字段名 | `number` | `fn_list_sum_field(orderList, 'amount')` |
| `fn_list_group` | 按指定字段将集合分组,返回Map结构: {字段值:元素集合} | 集合对象，字段名 | `map` | `fn_list_group(orderList, 'status')` |
| `fn_list_distinct_field` | 集合去重,传递字段名按字段值去重,不传按元素本身去重,保留首次出现顺序 | 集合对象，字段名(可选) | `list` | `fn_list_distinct_field(userList, 'userId')` |
| `fn_list_join_field` | 提取指定字段值并用分隔符拼接成字符串,默认逗号分割,空值忽略 | 集合对象，字段名，分隔符(可选,默认`,`) | `string` | `fn_list_join_field(orderList, 'orderId', ',')` |
| `fn_list_sort_field` | 按指定字段对集合排序返回新集合,默认升序 | 集合对象，字段名，排序方式(可选:asc/desc) | `list` | `fn_list_sort_field(orderList, 'amount', 'desc')` |
| `fn_list_contains` | 判断集合中是否包含指定值,数值跨精度比较,空集合返回false | 集合对象，判断的值 | `true \|\| false` | `fn_list_contains(userRoles, 'admin')` |
| `fn_list_merge` | 合并多个集合返回新集合,空集合会被忽略 | 集合1，集合2，集合N | `list` | `fn_list_merge(blackUsers, grayUsers)` |
| `fn_list_size` | 获取集合大小,空集合返回0 | 集合对象 | `number` | `fn_list_size(orderList)` |

---

## 内置变量 (BaseVariableEnums)

| 变量名 | 描述 | 返回类型 |
|--------|------|----------|
| `env_date_local_date_time` | 获取LocalDateTime对象 | `LocalDateTime` |
| `env_date_local_date` | 获取LocalDate对象 | `LocalDate` |

---

## 参考链接

- [Aviator 系统函数文档](https://www.yuque.com/boyan-avfmj/aviatorscript/ashevw)