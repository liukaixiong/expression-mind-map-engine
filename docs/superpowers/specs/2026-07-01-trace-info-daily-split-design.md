# Trace Info 日级分表设计

- **日期**: 2026-07-01
- **状态**: 已批准（Approved）
- **作者**: Claude Code（设计） / liukx（评审）
- **相关模块**: `expression-mind-map-server`

## 1. 背景与目标

服务端追踪日志分两张表存储：

- `expression_trace_log_index` —— 一次执行产生 **1 条**索引记录（数据量小）。
- `expression_trace_log_info` —— 一次执行产生 **N 条**明细记录（数据量大，是瓶颈所在）。

现状两张表都按**月度**拆分（`expression_trace_log_info_2607`）。`info` 表月度数据量仍然过大，需要按**天**拆分。

同时，页面端追踪查询目前无法按日期筛选，也无法跨拆分表查询历史明细。

### 目标

1. `info` 表改为按**天**拆分，缓解单表数据量；`index` 表保持按**月**拆分不变。
2. 列表查询：前端增加日期范围筛选；表路由**保持现状**（活跃表 + offset=-1）。
3. 详情查询：按 `created` 精确定位**单张** `info` 日表，避免跨表合并。
4. 历史月度 `info` 归档表中的旧明细**不迁移、不做混合兼容**——按日期算出的日表里查不到即视为查不到。

### 非目标

- 不引入动态表名拦截器（MyBatis-Plus `DynamicTableNameInnerInterceptor`）。写入无需感知分表；跨天合并查询由"两段式查询"规避。
- 不做存量月度 `info` 表的数据迁移脚本。
- 不引入 `default-scan-days` / `max-query-days` 等日期范围上限配置（详情查询只命中单表，列表查询路由不变，这两个上限失去意义，YAGNI）。

## 2. 关键设计决策（已与用户确认）

| # | 决策点 | 选择 | 说明 |
|---|--------|------|------|
| D1 | index 表分表粒度 | **保持月度** | index 数据量小，月度可接受 |
| D2 | info 表分表粒度 | **改为天级** | 解决数据量瓶颈 |
| D3 | 列表查询表路由 | **保持现状**（活跃表 + offset=-1 两张） | 不增加跨多表路由 |
| D4 | 列表查询日期过滤 | **新增** WHERE `created BETWEEN` | 兑现"页面按日期查询"原始需求；在 D3 的两张表内过滤 |
| D5 | 详情查询 `created` 来源 | 前端优先携带；缺省时**查一次 index 活跃表** | 常态走携带路径；兜底查 DB |
| D6 | 历史 info 旧明细查不到 | **返回无明细，不兜底兼容** | 不做混合表名解析 |
| D7 | `default-scan-days` / `max-query-days` | **删除**（不再需要） | 详情只命中单表，列表路由不变 |

## 3. 两段式查询总览

```
写入（一次执行：1 条 index + N 条 info，created 相同）
  index → expression_trace_log_index_2607      ← 月度活跃表
  info  → expression_trace_log_info_260701     ← 当天日级活跃表

列表查询 queryTraceLogList（页面表格，高频）
  仅查 index 月度分表（活跃 + offset=-1），WHERE created BETWEEN [start,end]
  完全不碰 info 分表

详情查询 getTraceInfo(traceLogId, created?)
  ① 用 created 算 index 月表名 → 按 id 查 index 记录
  ② 用 created 算 info  日表名 → 按 traceLogId 查明细
  精确命中两张表，零跨表合并
```

**核心优势**：`info`（大表）按天拆解决数据量；列表查询（高频）完全不碰 `info` 分表；详情查询按 `created` 精确定位单张日表——跨多天 `info` 合并分页的需求被彻底消除。

## 4. 组件改动清单

### 4.1 `TableSplitRule`（枚举，核心基建）

现状只有"按今日偏移"算表名的方法。新增"按绝对日期"算表名的方法，供详情查询路由使用。

```java
public enum TableSplitRule {
    month(...),  // 后缀 yyMM  （如 2607）
    day(...);    // 后缀 yyMMdd （如 260701）

    // 已有：按今日偏移算表名（写入/归档用）
    public String getFullTableName(String tableName, Integer offsetNumber);

    // 新增：按绝对日期算表名（详情查询路由用）
    // month → tableName_yyMM    例 expression_trace_log_info_2607
    // day   → tableName_yyMMdd  例 expression_trace_log_info_260701
    public String getFullTableNameByDate(String tableName, Date date);
}
```

枚举内部将"偏移规则"与"绝对日期规则"分别持有（两个 `Function`）。这是整套改动的核心基建，其余组件都依赖它。

### 4.2 `ExpressionServerProperties` / `application.yml`（配置）

```yaml
spring.expression.server.table-rule-list:
  - tableName: expression_trace_log_index
    tableSplitRule: month        # 不变
    max-save-number: 2           # 保留 2 个月（不变）
  - tableName: expression_trace_log_info
    tableSplitRule: day          # month → day  ← 唯一改动
    max-save-number: 30          # 语义由"月"变"天"，保留 30 天
```

`ExpressionServerProperties.TableRule` 结构不变；`max-save-number` 的语义随 `tableSplitRule` 变化（已由 `clearExpiredTableName` 按 `-maxSaveNumber-1` 偏移自然处理）。

### 4.3 `MysqlTableManager`（归档/清理，规则无关化）

- **重命名** `tableArchiveByMonth(tableName)` → `tableArchive(tableName)`。该方法本就规则无关（内部 `getTableName(tableName, -1)` 按各表配置的规则算偏移），名字带 "Month" 是误导。
- `getLastTableNameList(entityClass, offsetNumber)` / `clearExpiredTableName(tableName)` / `checkTableExist(tableName)` **不动**——已规则无关，`info` 配 `day` 后自动按天工作。
- **新增**（可选）按绝对日期查表名的辅助：
  ```java
  // 详情查询用：算出 created 对应的物理表名，并校验是否真实存在；不存在返回 null
  public String getTableNameByDate(Class<? extends BaseTableEntity> entityClass, Date date);
  ```

### 4.4 `ExpressionScheduledJob`（归档调度）

保留现有月度归档方法，**新增**每日归档方法：

```java
// 已有：每月1日凌晨执行，处理 TableSplitRule.month 的表（index）
@Scheduled(cron = "0 0 0 1 * ?")
public void executeMonthlyTableArchive() { ... manager.getTableNameList(month) ... }

// 新增：每天凌晨执行，处理 TableSplitRule.day 的表（info）
@Scheduled(cron = "0 0 0 * * ?")
@Transactional
public void executeDailyTableArchive() {
    // 逻辑与月度归档同构：遍历 day 规则的表，
    // tableArchive(tableName) 轮转 + clearExpiredTableName(tableName) 清理
}
```

两个方法都调用重命名后的 `manager.tableArchive(tableName)`，差异仅在 `getTableNameList(month)` vs `getTableNameList(day)` 与 cron 表达式。

> 集群部署注释保留（与现状一致，分布式锁为后续事项）。

### 4.5 `DefaultMysqlTraceLogStorageService`（查询路由）

**写入 `saveTraceLog`** —— 不动。`this.save(index)` + `saveBatch(info)` 写活跃基准表；轮转后昨天数据已被腾进归档表，今天新数据自然落进新空表。

**列表查询 `queryTraceLogList`** —— 表路由保持现状（D3），仅 WHERE 增加 `created BETWEEN`（D4）：

```java
wrapper.ge(queryRequest.getStartDate() != null, ExpressionTraceLogIndex::getCreated, queryRequest.getStartDate())
       .le(queryRequest.getEndDate()   != null, ExpressionTraceLogIndex::getCreated, queryRequest.getEndDate());
```

活跃表与 offset=-1 表的"补页"逻辑不动。

**详情查询 `getTraceInfo`** —— 重写为两段式精确路由（D5/D6）：

```java
public ExpressionTraceInfoDTO getTraceInfo(Long id, Date created) {
    Date actualCreated = created;
    ExpressionTraceLogIndex index;

    if (actualCreated != null) {
        // 常态路径：前端携带 created，按日期定位 index 月表
        String indexTable = tableManager.getTableNameByDate(ExpressionTraceLogIndex.class, actualCreated);
        index = (indexTable != null)
                ? baseMapper.selectByTable(indexTable, idWrapper(id))
                : null;
    } else {
        // 兜底路径：查 index 活跃表拿 created（D5 缺省策略）
        index = getById(id);
        actualCreated = (index != null) ? index.getCreated() : null;
    }

    if (index == null) return null;   // 查不到就查不到（D6）

    // 按 created 定位 info 日表（精确单表）
    List<ExpressionTraceLogInfo> infos = traceLogInfoService.getInfoListByTraceLogId(index.getId(), actualCreated);

    ExpressionTraceInfoDTO dto = new ExpressionTraceInfoDTO();
    BeanUtils.copyProperties(index, dto);
    dto.setTraceLogInfoList(infos);   // infos 可能为空（旧月表数据），符合 D6
    return dto;
}
```

> `TraceLogStorageService` 接口的 `getTraceInfo(Long id)` 签名扩展为 `getTraceInfo(Long id, Date created)`（`created` 可空）。

**`getExpressionSampleBody`** —— 不动（业务语义不受影响，仍走 `getById` 活跃表）。

### 4.6 `ExpressionTraceLogInfoServiceImpl`（明细按日期查询）

`getInfoListByTraceLogId` 增加 `created` 重载，按日期算 `info` 日表精确查询：

```java
// 新增重载：详情查询主路径
public List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId, Date created) {
    String infoTable = tableManager.getTableNameByDate(ExpressionTraceLogInfo.class, created);
    if (infoTable == null) return Collections.emptyList();   // 日表不存在 → 无明细（D6）
    LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper = ...eq(traceLogId, traceLogId);
    return getBaseMapper().selectListByTable(infoTable, wrapper);
}
```

> 旧的无 `created` 重载（`getInfoListByTraceLogId(Long traceLogId)`，offset=-1 兜底逻辑）**保留不动**，避免破坏既有调用方。新详情路径走带 `created` 的重载。

### 4.7 Mapper 层

`ExpressionTraceLogInfoMapper` 已有 `selectPageByTable`。**新增**按 id 列表查询的 `selectListByTable`（若不存在）：

```java
@Select("SELECT * FROM ${tableName} ${ew.customSqlSegment}")
List<ExpressionTraceLogInfo> selectListByTable(@Param("tableName") String tableName,
                                               @Param("ew") LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper);
```

`ExpressionTraceLogIndexMapper` **新增**按 id 在指定表查单条：

```java
@Select("SELECT * FROM ${tableName} ${ew.customSqlSegment}")
ExpressionTraceLogIndex selectByTable(@Param("tableName") String tableName,
                                      @Param("ew") LambdaQueryWrapper<ExpressionTraceLogIndex> wrapper);
```

两个 XML 文件当前为空壳，无需改动（注解 SQL 即可）。

### 4.8 请求 / 响应 DTO

`QueryExpressionTraceRequest` 新增两个日期字段：

```java
private Date startDate;   // created >=
private Date endDate;     // created <=
```

`/executor/trace/info` 接口签名扩展：

```
POST /expression-engine/executor/trace/info?id={id}&created={毫秒时间戳, 可选}
```

### 4.9 前端

**`trace-list.html`**：
- 表单增加 laydate 日期范围选择框，对应 `startDate` / `endDate`。
- "查看追踪信息"按钮 URL 拼上 `&created=` + 当前行 `created`（毫秒时间戳）。

**`expression-rule-config.html`**：
- 读取 URL 参数 `created`，调 `/info` 时带上（命中 D5 常态路径）。

**日期序列化**：`application.yml` 已配置 `write-dates-as-timestamps: true`，前后端按数值（毫秒）传递，无需额外格式处理。

### 4.10 影响面汇总

| 层 | 文件 | 改动 |
|----|------|------|
| 枚举 | `TableSplitRule.java` | 新增 `getFullTableNameByDate`（核心） |
| 配置 | `application.yml` | `info` 规则 `month→day`，`max-save-number=30` |
| Manager | `MysqlTableManager.java` | 重命名 `tableArchiveByMonth→tableArchive`；新增 `getTableNameByDate` |
| Job | `ExpressionScheduledJob.java` | 新增 `executeDailyTableArchive` |
| Mapper | `ExpressionTraceLogIndexMapper.java` | 新增 `selectByTable` |
| Mapper | `ExpressionTraceLogInfoMapper.java` | 新增 `selectListByTable`（若无） |
| Service | `DefaultMysqlTraceLogStorageService.java` | 列表加日期 WHERE；详情重写两段式 |
| Service | `ExpressionTraceLogInfoServiceImpl.java` | `getInfoListByTraceLogId` 增加 `created` 重载 |
| Service | `TraceLogStorageService.java`（接口） | `getTraceInfo` 签名扩展 |
| Controller | `ExecutorTraceController.java` | `/info` 增加 `created` 参数 |
| DTO | `QueryExpressionTraceRequest.java` | 新增 `startDate` / `endDate` |
| 前端 | `trace-list.html` | 日期范围框 + `created` 透传 |
| 前端 | `expression-rule-config.html` | 读取并透传 `created` |

写入 `saveTraceLog` 与列表表路由**零改动**。

## 5. 错误处理与边界

| 场景 | 行为 |
|------|------|
| 前端未带 `created` 调 `/info` | 兜底查 index 活跃表拿 `created`（D5） |
| index 活跃表查不到该 id | 返回 null（与现状一致，非回归） |
| 算出的 info 日表不存在（历史月表数据 / 表被清理） | `getTableNameByDate` 返回 null → 明细返回空列表（D6） |
| `startDate` / `endDate` 仅传一端 | 单边 `BETWEEN`（`ge` 或 `le`） |
| info 日归档失败 | 轮转原子操作（建 tmp→RENAME×2），失败抛异常回滚；当日数据仍写入旧活跃表，次日轮转时再腾挪 |

## 6. 测试计划

JUnit 4 + `SpringRunner`（项目既有测试框架）。

1. **`TableSplitRule.getFullTableNameByDate`**（纯逻辑单测）
   - month 规则 + 跨年日期 → `_yyMM`
   - day 规则 + 跨年日期 → `_yyMMdd`
   - 边界：月初 / 月末 / 年末
2. **详情路由 `getTraceInfo`**
   - `created` 有值且 index/info 日表均存在 → 正常返回明细
   - `created` 有值但 info 日表不存在 → 返回 index 记录 + 空明细列表
   - `created` 为空 → 走 `getById` 兜底拿 created
   - index 活跃表查不到 → 返回 null
3. **列表查询日期过滤**
   - 传 `startDate` / `endDate` → WHERE 含 `created BETWEEN`
   - 仅传一端 / 都不传 → 不报错
4. **日归档 job**（mock `tableArchiveMapper`）
   - 验证 RENAME 顺序（建 tmp→RENAME 旧表为归档→RENAME tmp 顶上）
   - 验证归档表名后缀为**昨天**的 `yyMMdd`
   - 验证 `clearExpiredTableName` 按 `maxSaveNumber=30` 清理

## 7. 风险

- **集群部署**：日归档 job 与月度归档 job 一样，目前无分布式锁（现有注释已标注）。单实例部署不受影响；集群场景下需后续加 Redisson 锁（与现状同级别的已知限制，非本次引入）。
- **`info` 表 `max-save-number` 语义变更**：从"2 个月"变为"30 天"，运维需知晓保留窗口缩短（30 天 vs 之前的约 2 个月）。可按需调大。
- **历史 info 月度旧明细不可查**：按 D6，改造前已归档进月度表的 `info` 明细，详情查询将返回空明细。属预期行为，已在非目标中声明。
