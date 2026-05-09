# 表达式规则引擎服务端

整体服务体系：`SpringBoot + Mybatis plus + Mysql + Redis `，都是常用组件，逻辑简单，二开不费力。

## 源码启动

1. 编译项目

- jdk8

> mvn clean install -Pjdk8 -Djava.version=8
>
> 或者去主pom文件里面将jdk8相关的注释解开，把jdk17的注释掉

- jdk 17(默认)

> mvn clean install

2. 初始化表结构SQL : `script/expression_mysql.sql`
3. 配置文件

- 非nacos的话需要指定Mysql和Redis的地址，可以在启动参数中添加 `engine.xxx`部分，或者自行补全`application.yml`内的engine相关的参数

- nacos则参考`script/expression-mind-map-server.yml` ，参考`bootstrap.yml`；

  > -Dapp.nacos.enable=true -Dapp.nacos.ip=127.0.0.1:8848 

4. 启动类: `BootApplication`



## 服务端拓展点

> 由于从内部独立出来花费比较长时间，有些代码写法需要优化，请见谅。
>
> 文档我也会尽量补充，有问题可直接联系本人即可。
>
> 后续计划将持久层再优化下，Mysql不适合日志存储。

### 1、配置类

```yaml
spring:
  expression:
    server:
      enable-login: true # 是否开启登录
      username: admin	 # 用户名
      password: 1234	 # 密码
```

### 2、关键类

| 名称                         | 作用                         | 默认实现类                       | 默认实现作用                                                 |
| ---------------------------- | ---------------------------- | -------------------------------- | ------------------------------------------------------------ |
| IExpressionLoginService      | 登录鉴权                     | ExpressionLoginServiceImpl       | 从配置中获取用户名密码进行验证，可以新写一个实现来替代该接口 |
| IExpressionTokenService      | token生成器                  | ExpressionMd5TokenServiceImpl    | 默认是基于MD5生成的，如果对此要求比较高，可以替换该接口即可  |
| TraceLogStorageService       | 追踪日志存储                 | DefaultMysqlTraceLogStorageService | 默认存储到 MySQL（含按月分表），可替换为 ES、ClickHouse 等  |
| ClientExecutorController     | 客户端与服务端交互的路由入口 |                                  |                                                              |
| ExecutorManagerController    | 执行器增删改入口             |                                  |                                                              |
| ExecutorExpressionController | 表达式增删改入口             |                                  |                                                              |
| ExecutorTraceController      | 执行器表达式的追踪入口       |                                  |                                                              |
| ComponentsController         | 执行器导入导出入口           |                                  |                                                              |

### 3、追踪日志存储扩展点（TraceLogStorageService）

默认情况下追踪日志存储在 MySQL 中（`expression_trace_log_index` + `expression_trace_log_info`），并通过 `MysqlTableManager` 实现按月分表。
如果日志量大或需要更强大的检索能力，可以实现 `TraceLogStorageService` 接口，将日志存储到 ES、ClickHouse 等引擎。

#### 接口方法

| 方法签名 | 说明 |
| --- | --- |
| `saveTraceLog(ExpressionExecutorResultDTO)` | 保存一条追踪日志（索引 + 明细） |
| `queryTraceLogList(QueryExpressionTraceRequest)` → `TraceLogPageResult` | 分页查询追踪日志列表 |
| `getTraceInfo(Long id)` → `ExpressionTraceInfoDTO` | 查询追踪详情（索引 + 明细） |
| `getExpressionSampleBody(Long expressionId)` → `ExpressionTraceLogIndex` | 获取表达式最近一次成功执行的样本请求体 |
| `getInfoListByTraceLogId(Long traceLogId)` → `List<ExpressionTraceLogInfo>` | 查询追踪日志明细列表 |
| `hasRecentlySuccessLog(Long expressionId, Date startDate)` → `boolean` | 判断表达式在指定日期后是否有成功记录 |

#### 替换方式

注册一个自定义的 Spring Bean 即可，框架通过 `@ConditionalOnMissingBean` 自动跳过默认的 MySQL 实现：

```java
@Service
public class ElasticsearchTraceStorageService implements TraceLogStorageService {

    @Override
    public void saveTraceLog(ExpressionExecutorResultDTO traceLog) {
        // 存储到 ES
    }

    @Override
    public TraceLogPageResult<ExpressionTraceLogIndex> queryTraceLogList(QueryExpressionTraceRequest queryRequest) {
        // 从 ES 查询
        return new TraceLogPageResult<>(records, total);
    }

    // ... 实现其余方法
}
```

> **注意：** `TraceLogStorageService` 接口不依赖 MyBatis 或任何 ORM 框架。分页返回值 `TraceLogPageResult` 是纯 POJO，接入方无需引入 MyBatis Plus。

> 服务端的职责：
>
> 1、维护执行器、规则等配置数据，
>
> 2、收集客户端的函数和变量定义以及表达式执行日志数据。
>
> 后半部分如果不太需要，可以直接让客户端直接连Redis即可，都不需要服务端，前提就是服务端要把数据更新到Redis中。具体参考客户端接入



## 其他

nacos相关版本介绍 : https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E



