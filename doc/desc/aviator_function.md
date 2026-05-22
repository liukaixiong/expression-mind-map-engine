### 🛠️ AviatorScript 函数库列表

#### 1. 系统函数
| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `assert(predicate, [msg])` | 断言函数，当 predicate 的结果为 false 的时候抛出 AssertFailed 异常，msg 错误信息可选。 | predicate, msg | 无 |
| `sysdate()` | 返回当前日期对象 java.util.Date | 无 | 无 |
| `rand()` | 返回一个介于 [0, 1) 的随机数，结果为 double 类型 | 无 | 无 |
| `rand(n)` | 返回一个介于 [0, n) 的随机数，结果为 long 类型 | n | 无 |
| `cmp(x, y)` | 比较 x 和 y 大小，返回整数，0 表示相等，1 表达式 x > y，负数则 x < y。 | x, y | 无 |
| `print([out],obj)` | 打印对象,如果指定 out 输出流，向 out 打印，默认输出到标准输出 | out, obj | 无 |
| `println([out],obj)` | 与 print 类似,但是在输出后换行 | out, obj | 无 |
| `p([out], obj)` | `println` 的简写形式 | out, obj | 无 |
| `pst([out], e)` | 等价于 e.printStackTrace()，打印异常堆栈，out 是可选的输出流，默认是标准错误输出 | out, e | 无 |
| `now()` | 返回 System.currentTimeMillis() 调用值 | 无 | 无 |
| `long(v)` | 将值转为 long 类型 | v | 无 |
| `double(v)` | 将值转为 double 类型 | v | 无 |
| `boolean(v)` | 将值的类型转为 boolean，除了 nil 和 false，其他都值都将转为布尔值 true。 | v | 无 |
| `str(v)` | 将值转为 string 类型，如果是 nil（或者 java null），会转成字符串 'null' | v | 无 |
| `bigint(x)` | 将值转为 bigint 类型 | x | 无 |
| `decimal(x)` | 将值转为 decimal 类型 | x | 无 |
| `identity(v)` | 返回参数 v 自身，用于跟 seq 库的高阶函数配合使用。 | v | 无 |
| `type(x)` | 返回参数 x 的类型，结果为字符串，如 string, long, double, bigint, decimal, function 等。Java 类则返回完整类名。 | x | 无 |
| `is_a(x, class)` | 当 x 是类 class 的一个实例的时候，返回 true，例如 `is_a("a", String)`，class 是类名。 | x, class | 无 |
| `is_def(x)` | 返回变量 x 是否已定义（包括定义为 nil），结果为布尔值 | x | 无 |
| `undef(x)` | “遗忘”变量 x，如果变量 x 已经定义，将取消定义。 | x | 无 |
| `range(start, end, [step])` | 创建一个范围，start 到 end 之间的整数范围，不包括 end，step 指定递增或者递减步幅。 | start, end, step | 无 |
| `tuple(x1, x2, ...)` | 创建一个 Object 数组，元素即为传入的参数列表。 | x1, x2... | 无 |
| `eval(script, [bindings], [cached])` | 对一段脚本文本 script 进行求值，等价于 AviatorEvaluator.execute(script, env, cached) | script, bindings, cached | 无 |
| `comparator(pred)` | 将一个谓词（返回布尔值）转化为 java.util.Comparator 对象，通常用于 sort 函数。 | pred | 无 |
| `max(x1, x2, x3, ...)` | 取所有参数中的最大值，比较规则遵循逻辑运算符规则。 | x1, x2... | 无 |
| `min(x1, x2, x3, ...)` | 取所有参数中的最小值，比较规则遵循逻辑运算符规则。 | x1, x2... | 无 |
| `constantly(x)` | 用于生成一个函数，它对任意（个数）参数的调用结果 x。 | x | 无 |

#### 2. 字符串函数
| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `date_to_string(date,format)` | 将 Date 对象转化化特定格式的字符串 | date, format | 无 |
| `string_to_date(source,format)` | 将特定格式的字符串转化为 Date 对 象 | source, format | 无 |
| `string.contains(s1,s2)` | 判断 s1 是否包含 s2,返回 Boolean | s1, s2 | 无 |
| `string.length(s)` | 求字符串长度,返回 Long | s | 无 |
| `string.startsWith(s1,s2)` | s1 是否以 s2 开始,返回 Boolean | s1, s2 | 无 |
| `string.endsWith(s1,s2)` | s1 是否以 s2 结尾,返回 Boolean | s1, s2 | 无 |
| `string.substring(s,begin[,end])` | 截取字符串 s,从 begin 到 end,如果忽略 end 的话,将从 begin 到结尾,与 java.util.String.substring 一样。 | s, begin, end | 无 |
| `string.indexOf(s1,s2)` | java 中的 s1.indexOf(s2),求 s2 在 s1 中 的起始索引位置,如果不存在为-1 | s1, s2 | 无 |
| `string.split(target,regex,[limit])` | Java 里的 String.split 方法一致 | target, regex, limit | 无 |
| `string.join(seq,seperator)` | 将集合 seq 里的元素以 seperator 为间隔 连接起来形成字符串 | seq, seperator | 无 |
| `string.replace_first(s,regex,replacement)` | Java 里的 String.replaceFirst 方法 | s, regex, replacement | 无 |
| `string.replace_all(s,regex,replacement)` | Java 里的 String.replaceAll 方法 | s, regex, replacement | 无 |

#### 3. 数学函数
| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `math.abs(d)` | 求 d 的绝对值 | d | 无 |
| `math.round(d)` | 四舍五入 | d | 无 |
| `math.floor(d)` | 向下取整 | d | 无 |
| `math.ceil(d)` | 向上取整 | d | 无 |
| `math.sqrt(d)` | 求 d 的平方根 | d | 无 |
| `math.pow(d1,d2)` | 求 d1 的 d2 次方 | d1, d2 | 无 |
| `math.log(d)` | 求 d 的自然对数 | d | 无 |
| `math.log10(d)` | 求 d 以 10 为底的对数 | d | 无 |
| `math.sin(d)` | 正弦函数 | d | 无 |
| `math.cos(d)` | 余弦函数 | d | 无 |
| `math.tan(d)` | 正切函数 | d | 无 |
| `math.atan(d)` | 反正切函数 | d | 无 |
| `math.acos(d)` | 反余弦函数 | d | 无 |
| `math.asin(d)` | 反正弦函数 | d | 无 |

#### 4. Sequence 函数（集合处理）
| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `repeat(n, x)` | 返回一个 List，将元素 x 重复 n 次组合而成。 | n, x | 无 |
| `repeatedly(n, f)` | 返回一个 List，将函数 f 重复调用 n 次的结果组合而成。 | n, f | 无 |
| `seq.array(clazz, e1, e2,e3, ...)` | 创建一个指定 clazz 类型的数组，并添加参数到这个数组并返回。 | clazz, e1... | 无 |
| `seq.array_of(clazz, size1, size2, ...sizes)` | 创建 clazz 类型的一维或多维数组，维度大小为 sizes 指定。 | clazz, sizes | 无 |
| `seq.list(p1, p2, p3, ...)` | 创建一个 java.util.ArrayList 实例，添加参数到这个集合并返回。 | p1, p2... | 无 |
| `seq.set(p1, p2, p3, ...)` | 创建一个 java.util.HashSet 实例，添加参数到这个集合并返回。 | p1, p2... | 无 |
| `seq.map(k1, v1, k2, v2, ...)` | 创建一个 java.util.HashMap 实例，参数要求偶数个，类似 k1,v1 这样成对作为 key-value 存入 map，返回集合。 | k1, v1... | 无 |
| `seq.entry(key, value)` | 创建 Map.Entry 对象，用于 map, filter 等函数 | key, value | 无 |
| `seq.keys(m)` | 返回 map 的 key 集合 | m | 无 |
| `seq.vals(m)` | 返回 map 的 value 集合 | m | 无 |
| `into(to_seq, from_seq)` | 将 from sequence 的元素使用 seq.add 函数逐一添加到了 to sequence 并返回最终的 to_seq | to_seq, from_seq | 无 |
| `seq.contains_key(map, key)` | 当 map 中存在 key 的时候（可能为 null），返回 true。 | map, key | 无 |
| `seq.add(coll, element)` | 往集合 coll 添加元素，集合可以是 java.util.Collection | coll, element | 无 |
| `seq.add(m, key, value)` | 往 Map 添加键值对 | m, key, value | 无 |
| `seq.add_all(seq1, seq2)` | 将集合 seq2 的元素全部添加到 seq1 | seq1, seq2 | 无 |
| `seq.put(coll, key, value)` | 类似 List.set(i, v)。用于设置 seq 在 key 位置的值为 value | coll, key, value | 无 |
| `seq.remove(coll, element)` | 从集合或者 hash map 中删除元素或者 key | coll, element | 无 |
| `seq.get(coll, element)` | 从 list、数组或者 hash-map 获取对应的元素值 | coll, element | 无 |
| `map(seq,fun)` | 将函数 fun 作用到集合 seq 每元素上, 返回新元素组成的集合 | seq, fun | 无 |
| `filter(seq,predicate)` | 将谓词 predicate 作用在集合的每个元素 上,返回谓词为 true 的元素组成的集合 | seq, predicate | 无 |
| `count(seq)` | 返回集合大小，seq 可以是数组，字符串，range ，List 等等 | seq | 无 |
| `is_empty(seq)` | 等价于 count(seq) == 0，当集合为空或者 nil，返回 true | seq | 无 |
| `distinct(seq)` | 返回 seq 去重后的结果集合。 | seq | 无 |
| `is_distinct(seq)` | 当 seq 没有重复元素的时候，返回 true，否则返回 false | seq | 无 |
| `concat(seq1, seq2)` | 将 seq1 和 seq2 “连接”，返回连接后的结果 | seq1, seq2 | 无 |
| `include(seq,element)` | 判断 element 是否在集合 seq 中,返回 boolean 值 | seq, element | 无 |
| `sort(seq, [comparator])` | 排序集合,仅对数组和 List 有效,返回排序后的新集合 | seq, comparator | 无 |
| `reverse(seq)` | 将集合元素逆序，返回新的集合。 | seq | 无 |
| `reduce(seq,fun,init)` | 将 fun 作用在结果值和集合的每个元素上面，返回最终的结果值 | seq, fun, init | 无 |
| `take_while(seq, pred)` | 从集合 seq 收集 pred 调用为 true 的元素。 | seq, pred | 无 |
| `drop_while(seq, pred)` | 丢弃任何 pred(x) 为 true 的元素并返回最终的结果集合。 | seq, pred | 无 |
| `group_by(seq, keyfn)` | 对集合 seq 的元素按照 keyfn(x) 的调用结果做分类，返回最终映射 map。 | seq, keyfn | 无 |
| `zipmap(keys, values)` | 返回一个 HashMap，其中按照 keys 和 values 两个集合的顺序映射键值对。 | keys, values | 无 |
| `seq.every(seq, fun)` | 当集合里的每个元素调用 fun 后都返回 true 的时候，整个调用结果为 true | seq, fun | 无 |
| `seq.not_any(seq, fun)` | 当集合里的每个元素调用 fun 后都返回 false 的时候，整个调用结果为 true | seq, fun | 无 |
| `seq.some(seq, fun)` | 当集合里的只要有一个元素调用 fun 后返回 true 的时候，整个调用结果立即为该元素 | seq, fun | 无 |
| `seq.eq(value)` | 返回一个谓词,用来判断传入的参数是否跟 value 相等 | value | 无 |
| `seq.neq(value)` | 返回判断不等于的谓词 | value | 无 |
| `seq.gt(value)` | 返回判断大于 value 的谓词 | value | 无 |
| `seq.ge(value)` | 返回判断大于等于 value 的谓词 | value | 无 |
| `seq.lt(value)` | 返回判断小于 value 的谓词 | value | 无 |
| `seq.le(value)` | 返回判断小于等于 value 的谓词 | value | 无 |
| `seq.nil()` | 返回判断是否为 nil 的谓词 | 无 | 无 |
| `seq.exists()` | 返回判断不为 nil 的谓词 | 无 | 无 |
| `seq.and(p1, p2, p3, ...)` | 组合多个谓词函数，返回一个新的谓词函数，当所有函数都返回 true 的时候，新函数返回 true | p1, p2... | 无 |
| `seq.or(p1, p2, p3, ...)` | 组合多个谓词函数，当其中一个返回 true 的时候，新函数立即返回 true | p1, p2... | 无 |
| `seq.min(coll)` | 返回集合中的最小元素，要求集合元素可比较 | coll | 无 |
| `seq.max(coll)` | 返回集合中的最大元素，要求集合元素可比较 | coll | 无 |

#### 5. 模块加载
| 函数名 | 描述 | 参数 | 示例 |
| :--- | :--- | :--- | :--- |
| `load(path)` | 加载 path 路径指定的模块，每次都将重新编译该模块文件并返回 exports | path | 无 |
| `require(path)` | 加载 path 路径指定的模块，如果已经有缓存，将直接返回结果，否则将编译并缓存 exports | path | 无 |
