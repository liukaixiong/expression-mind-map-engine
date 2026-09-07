---
name: expression-branch-planner
description: 在表达式引擎控制台里,根据业务提示规划表达式规则分支树并按批准执行创建/修改。当用户要求"规划分支/新建表达式分支/修改规则逻辑/分析现有规则树"时使用。任何改动必须先输出改动方案让用户批准,严禁未经批准直接应用。
---

# 表达式分支规划器(Expression Branch Planner)

通过控制台 HTTP API,在表达式引擎的规则树里:分析现有分支 → 根据业务提示规划改动方案(新增分支/修改节点)→ 用户批准 → 执行 → 验证 → 汇报。

如需页面级操作或截图验收,可借助浏览器自动化工具(如 ego,可继承用户登录态),但核心流程全部走 HTTP API,不依赖浏览器。

## 核心原则

- **批准门(最高优先级)**:任何写操作(addOne/editOne/editParentId/batchDelete)之前,必须先向用户完整展示改动方案并获得明确批准。**严禁未经批准直接应用,严禁凭猜测应用。**
- **一切改动都需授权**:新增、修改、删除一律走批准门,没有例外。用户没明说时按"新增分支"理解,但新增同样要先出方案获批准;修改还必须额外展示**修改前后对比**。
- **启动自检**:skill 一经使用,先核对配置是否齐全匹配,不匹配必须一次性告知用户哪个文件哪个字段缺什么(见第一步),补齐前不得往下执行。
- **环境无关**:控制台地址、账密等环境信息不写入任何会提交到仓库的文件,从用户消息或本地配置解析(见第一步)。
- **无状态**:不依赖任何上一次会话的浏览器状态、变量或文件,每次从头执行;中间产物写 `/tmp/` 但下次会话不得复用;认证 token 只存内存。
- 认证 token、账密不得写入任何文件;若用了浏览器工具,结束前关闭任务空间(用户要看页面则保留)。

## 第一步:启动自检与认证

### 1. 配置核对(必做,通过才继续)

逐项核对,任一项不满足就**一次性列出全部缺口**告知用户,明确指出**哪个文件、哪个字段、需要补什么**,用户消息里已提供的信息可跳过对应项:

| 检查项 | 读取位置 | 不满足时的告知话术要点 |
|---|---|---|
| 本地配置文件存在 | `.agents/console.local.json` | "缺少该文件,请复制 `.agents/console.local.json.example` 为 `.agents/console.local.json` 并填写" |
| baseUrl 已填且非占位符 | 同上 `baseUrl` 字段 | "该文件 `baseUrl` 字段为空/仍是占位符,请填控制台地址" |
| 账密已填(可选) | 同上 `username`/`password` | 未填不是错误,但必须告知:"当前无法自动登录,将使用浏览器登录态兜底;如需全自动,请补充这两个字段" |
| 控制台可达 | GET `<baseUrl>`(带认证探测任一接口更佳) | "地址无法访问,请核对 baseUrl 是否正确、网络/VPN 是否可达" |

- 用户消息里直接给了地址/账密的,以用户消息为准,跳过对应配置项;
- **配置不齐时禁止继续执行后续步骤**,把缺口列全等用户补充,不要挤牙膏式反复问。

### 2. 控制台 base URL(只在内存中使用)

1. 用户消息里给了地址/链接,直接用;
2. `.agents/console.local.json` 的 `baseUrl`;
3. 都没有 → 询问用户,不要猜。

### 3. 认证(按优先级,必须以具体用户身份操作)

1. **账密自动登录(推荐)**:`console.local.json` 有 `username`/`password`,或用户消息提供账密 → `POST /login`(form: username/password)→ 返回 `{tokenName, tokenValue}` → 后续请求带该 Cookie(或同名 Header)。审计上每个节点会记到该用户名下。
2. **浏览器登录态**:已登录的浏览器上下文(如 ego 任务空间)里用 `browserFetch` 自动带 Cookie;未登录则交接给用户手动登录,用户确认后继续。不要尝试默认账密(无效)。

**不要使用 API Key(X-Api-Key)方式**操作:该方式审计身份统一记为"API客户端",无法追溯分支到底是谁规划/创建的。

## 输入约定:用户如何指定目标节点

1. **节点 id(最准)**:画布上节点标题前的【数字】,如"在 3652 下加一个xx分支";
2. **配置页 URL + 节点描述**:`expression-rule-config.html?executorId=...` 链接 + 标题描述("规则分配那个节点下"),拉树后模糊匹配;
3. **只给执行器**:executorId / executorCode / 规则配置页 URL(从 query 解析)。先拉整棵树展示缩进树,让用户指认目标节点。

匹配到多个候选或匹配不到时,列出来让用户选,不要猜。

### 光杆节点 id 的上下文还原流程

用户只给一个节点 id(如"3652")时,按以下顺序还原上下文,**必须全部完成后才开始规划**:

1. **定位节点**:`POST /expression-engine/executor/expression/findExpressionInfo?id=<id>`(只按 id 查,无需 executorId)→ 拿到节点本身及其 `executorId`。注意:返回的 `id` 是字符串、`parentId` 是数字,组树前统一 `Number()` 规范化;
2. **拉整棵树**:`findExpressionList`(用上一步的 executorId)→ 还原三类上下文,缺一不可:
   - **祖先链**:从根到该节点的路径(理解它在哪个业务域、挂在哪个分组下);
   - **子树**:它名下所有后代(理解它当前承担的职责,新分支不能与现有子节点重复/冲突);
   - **兄弟节点及 priorityOrder**:理解同级执行顺序与已占用的分支位;
3. **可选增强**:拉追踪样本(`trace/list`)看该执行器真实入参;若要理解某表达式里变量(如 `teacherInfo`)从哪来,沿祖先链+子树按执行顺序搜 `fn_env_put_value('变量名',...)` 的定义节点;
4. 向用户复述理解:"节点 X(标题)在…之下,当前负责…,同级还有…,我理解你要在这里…",确认后再进规划。

## 第二步:拉取现有树(分析的事实基础)

```js
POST /expression-engine/executor/expression/findExpressionList
body: { "executorId": <id>, "traceLogId": null, "traceLogCreated": null, "missStartDate": null, "id": null }
```

返回节点数组,字段:`id, parentId, expressionType, expressionCode, expressionTitle, expressionDescription, expressionContent, expressionStatus, priorityOrder, configurabilityJson`。按 parentId 组树后打印缩进树(类型 + 标题 + 表达式摘要)给用户看。

## 引擎领域知识(规划时必须遵守)

**执行语义**(源码:`expression-mind-map-client-starter` 的 `LocalEngineServiceImpl`):
1. 深度优先先序遍历;节点表达式结果为 `Boolean true` 才执行其子节点,false/非 Boolean 跳过整棵子树;
2. 兄弟节点按 `priorityOrder` **降序**执行,相同时按 id 升序;
3. `expressionType`(condition/action/trigger/callback)只是配置层语义标签,运行时行为一致。

**节点类型惯例**(与画布颜色一致):
- `action` + `content=true`:根/分组节点,纯分组不承载逻辑;
- `condition`:分支守卫(条件表达式),命中后执行其子节点;
- `trigger`:动作节点(初始化、写变量、记出参、终止),通常挂在 condition 下;
- `callback`:回调类动作。

**流程控制函数**(可短路拼在 `&&` 尾部):
- `fn_in_end()` / `fn_end()`:命中即止分支的标准结尾——命中本分支后终止整棵树;
- `fn_error_message('原因')`:兜底/拦截——写失败信息到结果上下文并强制终止;
- `fn_return()`:结束本级兄弟、回到父层继续(只能用在**无子节点**的节点);
- `fn_force_end()`:立即全局终止(慎用)。

**跨节点传值与出参**:
- `fn_env_put_value('key', value)`:定义变量,**下游节点**(priorityOrder 更小者)直接引用 key;
- `fn_record_result_context('k', v, ...)`:写业务出参;
- 变量来源:全局变量文档(`var` 类)、追踪样本入参字段、上游节点的 `fn_env_put_value`。**严禁编造函数或变量**——不确定时先拉文档或样本确认(见第三步)。

**规划规范**:

- **单一职责(核心规划观)**:
  - 一个节点只做一件事:要么是**判断**(condition 守卫),要么是**一个动作**(取数/过滤/清洗/写变量/记出参/终止),不要在一个节点里混杂多件事;
  - 一个分支聚焦一个业务关注点;节点标题用**抽象的职责语言**命名,如「初始化上下文数据」「过滤无效线索」「数据清洗」「轮询分配班主任」「记录分配结果」「兜底终止」,而不是贴实现细节;
  - 一条表达式里同时出现 取数 + 判断 + 写多个不相关变量 时,必须拆成多个同级子节点按序执行——每个环节独立成节点,追踪日志里命中路径才一目了然,后续单点替换/删除才不影响其他环节;
  - 推荐分支骨架:`condition(业务守卫) → trigger(初始化上下文数据) → trigger(过滤/数据清洗) → trigger(业务计算/分配) → trigger(记录出参)`,守卫不满足的兜底分支用 `fn_error_message` 或 `fn_in_end` 收尾;
- 分支尽量 MECE,**必须规划兜底分支**(未找到/为空/不在范围等,用 `fn_error_message` 或 `fn_in_end`);
- 先有"命中即返回"的快捷分支,再走主处理逻辑,最后兜底;
- 每个节点标题用业务语言(≤50 字);同级节点数建议 ≤5,深度建议 ≤4。

**范例**(命中即止 + 出参记录的典型写法):
```
[condition] 如果当前绑定了班主任并且是有效的
   content: boolean(env_user_info_obj.classTeacherId)
            && boolean(sys_user_get_info('userId', env_user_info_obj.classTeacherId))
            && fn_in_end()
   └─ [trigger] 返回绑定的班主任
        content: fn_record_result_context('sysUserInfo', sys_user_get_info('userId', env_user_info_obj.classTeacherId))
```

## 第三步:函数与变量盘点(规划表达式前必做)

**本 skill 不依赖本地仓库存在**:函数/变量是业务服务启动时注册、控制台透出的运行时名单,以下 1/2/3 均为纯远程来源,足以独立完成盘点;本地仓库(若恰好有)仅作第 4 级补充。

**不清楚有哪些可用函数/变量时,严禁开始规划表达式。**

### 全量拉取(首次触达某执行器/项目时,一次性完成)

用户首次提问任何表达式相关问题时,先做一次全量盘点,为整个会话建好"词表":

1. `GET /expression-engine/doc/getList?executorId=<id>&limit=500`(注意是 GET;limit 默认 10,必须显式调大);
2. 若返回条数达到 limit,说明没拉完——继续调大 limit 或分页,直到取全;
3. 同时拿全函数(type=fn)和变量(type=var);
4. **按 serviceName 缓存于会话内存**:文档是按项目(绑定服务)组织的,同会话内再遇到同一服务的其他执行器直接复用这份词表,不重复拉;
5. 向用户简报盘点结果(函数 N 个、变量 M 个、主要分组),表示上下文已就绪。

会话内后续所有表达式编写/规划都以这份全量词表为基准;`&name=xxx` 按名查询仅用于补充个别函数的语义细节(params/example),不做主数据源。

### 逐级确认顺序(词表之外的补充来源)

1. **追踪样本看真实数据结构**:`POST /expression-engine/executor/trace/list`,body `{executorId, pageNum:1, pageSize:5, orderByColumn:"created", isAsc:"desc"}`,取返回的 `envBody`——入参字段名、层级、类型以样本为准,文档没写的字段不要用;
2. **现有树表达式(经验源)**:该执行器既有节点的 `expressionContent` 里已在使用、且线上运行正常的函数/变量,可视为可用,并可直接参考其调用姿势;
3. **本地仓库文档(可选,仅当存在时)**:读本仓库 `doc/FUNCTION.md`、`doc/desc/` 下的函数说明——**没有仓库就跳过,不影响盘点**;
4. **上游节点定义的变量**:现有树或本次规划中,`fn_env_put_value('key', ...)` 定义的 key(按执行顺序,下游才可引用);
5. 以上都查不到 → **直接问用户**,把候选需求描述给用户确认。

> 引擎层函数(fn_in_end/fn_error_message/fn_env_put_value 等)的语义已固化在本 skill 的「引擎领域知识」一节,无需查任何外部资料。

盘点结果沉淀为「本次规划可用函数/变量清单」,在改动方案中原样展示(见第四步),证明没有编造。

## 第四步:生成改动方案并申请批准(人工门,不可跳过)

向用户完整展示**改动方案**,包括:

1. **意图说明**:本次规划解决什么业务问题,分支设计思路,兜底在哪;
2. **改动清单**,按类型分节呈现:
   - 新增:完整分支树(缩进树展示 类型 + 标题 + 父子关系 + 每节点完整表达式 + 挂载点);
   - 修改(仅当用户要求):逐节点列出 **修改前 → 修改后** 的表达式/标题/描述/优先级对比;
   - 删除(仅当用户明确要求):节点 id + 标题 + 影响范围(其子树);
3. **用到的函数/变量清单**,逐项标注出处(函数文档 / 追踪样本 / 上游 fn_env_put_value / 用户确认),证明没有编造;
4. 明确询问"是否批准应用"。用户要调整就改完重新展示,**获得明确批准前不得调用任何写接口**。

## 第五步:执行改动

**新增**(先父后子)逐节点 `POST /expression-engine/executor/expression/addOne`:

```json
{
  "executorId": 150,
  "parentId": 3649,
  "expressionType": "trigger",
  "expressionCode": "唯一编码(自定义,如 branch_xxx_01)",
  "expressionTitle": "节点标题",
  "expressionContent": "aviator 表达式",
  "expressionDescription": "AI 规划:<一句话意图>",
  "expressionStatus": 0,
  "priorityOrder": null
}
```

**修改**(仅限用户批准过的节点)`POST /expression-engine/executor/expression/editOne`,body 需带节点 `id`。

**通用规则**:
- `parentId`:新增首层用挂载节点 id,子节点用上一轮返回的 `data.id`;
- `expressionCode` 重复会报错——报错就换一个再试;
- **同级顺序** = `priorityOrder` 降序:先执行的兄弟给更大的值(建议首层从 10 起递减);规划了顺序就必须显式给值,否则退化为按 id 升序;
- 逐条记录执行结果与新建节点 id;中途失败立即停止,向用户汇报已完成部分,回滚(`POST /expression-engine/executor/expression/batchDelete`,body `{idList:[...]}`)**须先征得用户同意**;
- 执行结果与批准的方案不一致时(如实际建多了/建错了),如实告知,不擅自补救。

## 第六步:验证与汇报

1. 重新拉取树,确认拓扑/内容与批准方案一致;
2. 可选(有追踪样本时):`POST /expression-engine/debug/execute` 远程试跑,看新分支是否按预期命中;
3. 汇报:新建/修改节点 id 清单、每节点表达式、试跑命中情况;有浏览器条件时可打开配置页截图(`expression-rule-config.html?executorId=<id>&&executorCode=<code>`),没有则贴最终缩进树。

## 红线

- **任何写操作前必须拿到用户对改动方案的明确批准;严禁瞎猜、严禁先斩后奏;**
- 严禁编造函数/变量;按第三步盘点流程逐级确认,仍不确定就问用户;
- 修改/删除既有节点只能发生在用户明确要求且批准了具体改动的范围内;批准范围外发现的问题,汇报给用户另立方案;
- 控制台地址、账密等环境信息不得写入任何会提交到仓库的文件;
- 目标节点、业务含义有歧义时,先问用户,不要猜。
