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
| `fn_object_is_not_null` | 判断值是否为空，允许传递多个值，请传递变量 | 值1，值2 | `true \|\| false` | `fn_object_is_not_null(a1,a2)` |
| `fn_str_to_json` | 字符串转json对象 | json字符串 | `true \|\| false` | `fn_str_to_json(jsonStr)` |

---

## 内置变量 (BaseVariableEnums)

| 变量名 | 描述 | 返回类型 |
|--------|------|----------|
| `env_date_local_date_time` | 获取LocalDateTime对象 | `LocalDateTime` |
| `env_date_local_date` | 获取LocalDate对象 | `LocalDate` |

---

## 参考链接

- [Aviator 系统函数文档](https://www.yuque.com/boyan-avfmj/aviatorscript/ashevw)