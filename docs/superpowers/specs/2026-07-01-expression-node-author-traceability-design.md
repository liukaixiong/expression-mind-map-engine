# 表达式节点创建人/修改人溯源 设计文档

- 日期：2026-07-01
- 模块：`expression-mind-map-server`
- 作者：刘凯雄

## 1. 背景与目标

思维导图页（`expression-rule-config.html`）点击某个分支节点时，看不到该节点由谁创建、谁修改；编辑分支弹窗（`expressionForm.html`）里的"查看历史版本"列表/详情中，**操作人（operator）一直是空的**。

根因探查发现：

1. **节点审计字段已落库**：`ExpressionExecutorInfoConfig` 实体有 `createBy/updateBy/createTime/updateTime`，由 `AutoFillMetaObjectHandler` 在 insert/update 时用 `ExpressionUserContext.currentUsername()` 自动填充；DTO `ExpressionExecutorDetailConfigDTO` 也已携带这四个字段返回前端，思维导图节点 item 里本就有这些数据。**问题只在"前端没展示"。**
2. **历史版本 operator 为空**：`ExpressionConfigServiceImpl` 的 `addExpression/editExpression/batchDeleteByIdList` 调用 `saveHistory(expr, type, request.getCreateBy() / editRequest.getUpdateBy() / delRequest.getUpdateBy())`，但这些值由前端请求体传入；而前端编辑表单 `expressionForm.html:364` 提交的是纯表单 field，**根本不包含 createBy/updateBy**，导致 operator 恒为 null。

### 目标

1. 思维导图页点击节点时，以**悬浮信息卡**展示：创建人、创建时间、修改人、修改时间。
2. 编辑分支弹窗历史版本的 operator 始终为真实操作人。
3. **强制约束**：操作人必须从服务端 token（登录上下文）获取，**禁止由前端参数透传**——杜绝篡改。

### 非目标

- 不做节点级"多版本时间线对比"。
- 不改造除"取人逻辑"以外的历史版本业务功能。
- 不引入新的登录/鉴权机制。

## 2. 关键决策

| 决策 | 结论 | 理由 |
|------|------|------|
| 取人来源 | 服务端 `ExpressionUserContext` 统一获取，**删除前端透传入口** | 溯源必须可信，参数可被篡改 |
| 未登录（`enable-login=false`）兜底 | 返回固定系统账号 `"system"` | 保证字段非空、溯源链不中断；符合用户确认 |
| 自动填充行为 | `AutoFillMetaObjectHandler` 用兜底方法，null → `"system"` | 全局一致，所有实体受益 |
| 展示位置 | 思维导图节点点击悬浮卡 | 用户确认；不挤占导图布局 |
| 卡片字段 | 创建人+创建时间、修改人+修改时间（共四字段） | 用户确认"都加上"，溯源完整 |

## 3. 总体方案（方案 A）

收敛"取当前操作人"逻辑到 `ExpressionUserContext` 一个工具方法，自动填充与历史版本 operator 都走它；删除前端可传的 createBy/updateBy 入口；前端只做展示。

```
登录请求 → LoginHandler → ExpressionUserContext.set(user)   [已有]
                                    │
   ┌────────────────────────────────┼───────────────────────────┐
   ▼                                ▼                           ▼
AutoFillMetaObjectHandler     ExpressionConfigServiceImpl    (其他业务也复用)
insert/update 填              add/edit/delete 调
creator/updater/createBy/     saveHistory(expr, type)
updateBy                                         │
   │                                             ▼
   └────────────► ExpressionUserContext.currentUsernameOrSystem() ◄── 统一出口
                         登录→真实 username；未登录→"system"

思维导图节点 item（DTO 已带四字段）→ 点击 → 悬浮卡渲染（前端，零额外请求）
历史版本 operator（DB 已存真实值）→ 列表/详情直接展示
```

**核心收益**：取人逻辑单一、全局一致、根除篡改；历史版本 operator 空值问题随根因消除；前端仅做展示。

## 4. 详细设计

### 4.1 `ExpressionUserContext` 统一取人

**文件**：`expression-mind-map-core/.../core/model/ExpressionUserContext.java`

新增方法：

```java
/**
 * 系统默认账号：未开启登录（enable-login=false）时的兜底操作人。
 */
public static final String SYSTEM_OPERATOR = "system";

/**
 * 获取当前操作人：登录时返回真实 username；未登录返回 {@link #SYSTEM_OPERATOR}。
 * 用于所有审计字段填充与历史版本 operator，保证非空且来源可信（token / 上下文）。
 */
public static String currentUsernameOrSystem() {
    String username = currentUsername();
    return username != null ? username : SYSTEM_OPERATOR;
}
```

> 该类在 `expression-mind-map-core`，server 与 client 均可复用，无新依赖。

### 4.2 `AutoFillMetaObjectHandler` 兜底填充

**文件**：`expression-mind-map-server/.../config/AutoFillMetaObjectHandler.java`

现状 `currentUsername()` 为 null 时不填字段。改为始终用 `currentUsernameOrSystem()`：

```java
@Override
public void insertFill(MetaObject metaObject) {
    String username = ExpressionUserContext.currentUsernameOrSystem();
    this.setFieldValByName("creator", username, metaObject);
    this.setFieldValByName("updater", username, metaObject);
    this.setFieldValByName("createBy", username, metaObject);
    this.setFieldValByName("updateBy", username, metaObject);
    this.setFieldValByName("created", new Date(), metaObject);
    this.setFieldValByName("updated", new Date(), metaObject);
}

@Override
public void updateFill(MetaObject metaObject) {
    String username = ExpressionUserContext.currentUsernameOrSystem();
    this.setFieldValByName("updater", username, metaObject);
    this.setFieldValByName("updateBy", username, metaObject);
    this.setFieldValByName("updated", new Date(), metaObject);
}
```

> 去掉 `if (username != null)` 守卫，因为兜底后永不为 null。行为变化仅为：未登录场景从"留空"变为"system"。

### 4.3 `ExpressionHistoryVersionService` 取人内聚

**文件**：`.../service/ExpressionHistoryVersionService.java`、`.../service/impl/ExpressionHistoryVersionServiceImpl.java`

签名去掉 operator 参数，内部自行取：

```java
// 接口
void saveHistory(ExpressionExecutorInfoConfig expression, String changeType);

// 实现
@Override
public void saveHistory(ExpressionExecutorInfoConfig expression, String changeType) {
    ...
    historyVersion.setOperator(ExpressionUserContext.currentUsernameOrSystem());
    ...
}
```

### 4.4 `ExpressionConfigServiceImpl` 调用点改造

**文件**：`.../service/impl/ExpressionConfigServiceImpl.java`

三处调用去掉 operator 实参：

```java
// addExpression（~line 100）
expressionHistoryVersionService.saveHistory(expressionExecutorDetailConfig, "CREATE");

// editExpression（~line 162）
expressionHistoryVersionService.saveHistory(updatedConfig, "UPDATE");

// batchDeleteByIdList（~line 290）
expressionHistoryVersionService.saveHistory(expression, "DELETE");
```

### 4.5 封堵前端透传入口（安全收口）

**直接删字段的前提是不被其他业务引用。** 全局检索发现 `DeleteByIdListRequest.updateBy` 被多处逻辑删除链路复用：

- `ExpressionConfigServiceImpl:282`（表达式逻辑删除）
- `ExpressionExecutorConfigServiceImpl:166`（执行器逻辑删除）
- `ExpressionGlobalTraceLogServiceImpl:95`（全局追踪日志逻辑删除）

这些地方都用 `LambdaUpdateWrapper.set(updateBy, delRequest.getUpdateBy())` 把前端透传值写库——**同样是篡改隐患**。因此 `DeleteByIdListRequest.updateBy` 不能删（会破坏编译），但必须**忽略前端值、统一用上下文值**。

处理策略分两类：

**A. 可安全删除的 DTO 字段（仅历史版本链路使用，无其他引用）：**

| 文件 | 删除字段 | 理由 |
|------|----------|------|
| `AddExpressionConfigRequest.java` | `private String createBy;` | 仅 `addExpression` 用于 saveHistory，已改为内部取人 |
| `EditExpressionConfigRequest.java` | `private String updateBy;` | 仅 `editExpression` 用于 saveHistory，已改为内部取人 |

**B. 保留字段但服务端覆盖（多业务共用，删字段会破坏编译）：**

`DeleteByIdListRequest.updateBy` 保留，但三处逻辑删除的 `LambdaUpdateWrapper` 改用上下文值：

```java
// ExpressionConfigServiceImpl:282 等
.set(ExpressionExecutorInfoConfig::getUpdateBy,
     ExpressionUserContext.currentUsernameOrSystem())
```

> 这样前端无论传什么 `updateBy`，写库的始终是上下文真实操作人，彻底封堵篡改。同时不破坏其他业务的 DTO 契约。

### 4.6 前端：思维导图节点悬浮信息卡

**文件**：`expression-mind-map-server/src/main/resources/template/expression-rule-config.html`

复用 jsmind `select_node` 事件（`readerMind` 内，~line 986 现仅 `console.info`）。点击节点时用 `layer.tips` 或 `layer.open` 渲染卡片，数据取自已绑定的节点 item（`item.createBy / createTime / updateBy / updateTime`），**不发额外请求**：

```javascript
if (node.evt === 'select_node') {
    var n = jm.get_node(node.node);
    if (n && n.data) {
        var d = n.data;
        var html = '<div style="padding:8px 12px;font-size:13px;line-height:1.9;">'
            + '<div>创建人：' + (d.createBy || '-') + '</div>'
            + '<div>创建时间：' + (d.createTime || '-') + '</div>'
            + '<div>修改人：' + (d.updateBy || '-') + '</div>'
            + '<div>修改时间：' + (d.updateTime || '-') + '</div>'
            + '</div>';
        layer.tips(html, '#jsmind_container', {time: 4000, tips: [1, '#fff'], area: 'auto'});
    }
}
```

> 根节点（`isroot`）无审计字段，跳过展示。

### 4.7 前端：历史版本展示（无需改后端返回）

`expressionForm.html` 历史版本列表「操作人」列（`item.operator || '-'`，line 452）与详情预览「操作人」行（`history.operator || '-'`，line 501）**展示逻辑不动**——后端修好后 operator 即为真实值，自然显示。

## 5. 改动清单

| 层 | 文件 | 改动 |
|----|------|------|
| core | `ExpressionUserContext.java` | 新增 `SYSTEM_OPERATOR` 常量 + `currentUsernameOrSystem()` |
| server/config | `AutoFillMetaObjectHandler.java` | insert/update 改用兜底方法，去 null 守卫 |
| server/service | `ExpressionHistoryVersionService(Impl).java` | `saveHistory` 去掉 operator 参数，内部取人 |
| server/service/impl | `ExpressionConfigServiceImpl.java` | saveHistory 三处调用去实参；逻辑删除 updateBy 改用上下文值 |
| server/service/impl | `ExpressionExecutorConfigServiceImpl.java` | 逻辑删除 updateBy 改用上下文值（`:166`） |
| server/service/impl | `ExpressionGlobalTraceLogServiceImpl.java` | 逻辑删除 updateBy 改用上下文值（`:95`） |
| server/dto | `AddExpressionConfigRequest.java` | 删 `createBy`（无其他引用） |
| server/dto | `EditExpressionConfigRequest.java` | 删 `updateBy`（无其他引用） |
| server/dto | `DeleteByIdListRequest.java` | 保留字段，服务端忽略其值（多业务共用） |
| 前端 | `expression-rule-config.html` | `select_node` 事件加悬浮卡 |
| 前端 | `expressionForm.html` | 无改动（后端修好即正确） |

## 6. 风险与验证

- **逻辑删除 updateBy 统一覆盖**：三处 `LambdaUpdateWrapper.set(updateBy, ...)` 改用上下文值后，前端透传的 `updateBy` 被忽略，行为一致；编译保证不破坏（字段保留）。
- **未登录场景行为变化**：null → "system"，需确认不影响依赖 null 的查询/展示（现状展示均为 `|| '-'`，无 null 语义依赖）。
- **验证方式**：
  1. 编译通过 + 既有单测不回归。
  2. 开启登录：新建/编辑/删除表达式 → 历史版本 operator 为当前用户；思维导图点击节点卡片显示真实创建/修改人；执行器/全局追踪日志逻辑删除后 updateBy 为当前用户。
  3. 关闭登录（`enable-login=false`）：同样操作 → operator/审计字段为 "system"，非空。
  4. 尝试请求体带 `createBy/updateBy` → 服务端忽略，值仍为上下文/系统账号（验证不可篡改）。
