# 表达式规则引擎服务端

整体服务体系：`SpringBoot + Mybatis plus + Mysql + Redis `，都是常用组件，逻辑简单，二开不费力。

## 源码启动

1. 编译项目

`maven install `

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

| 名称                         | 作用                         | 默认实现类                    | 默认实现作用                                                 |
| ---------------------------- | ---------------------------- | ----------------------------- | ------------------------------------------------------------ |
| IExpressionLoginService      | 登录鉴权                     | ExpressionLoginServiceImpl    | 从配置中获取用户名密码进行验证，可以新写一个实现来替代该接口 |
| IExpressionTokenService      | token生成器                  | ExpressionMd5TokenServiceImpl | 默认是基于MD5生成的，如果对此要求比较高，可以替换该接口即可  |
| ClientExecutorController     | 客户端与服务端交互的路由入口 |                               |                                                              |
| ExecutorManagerController    | 执行器增删改入口             |                               |                                                              |
| ExecutorExpressionController | 表达式增删改入口             |                               |                                                              |
| ExecutorTraceController      | 执行器表达式的追踪入口       |                               |                                                              |
| ComponentsController         | 执行器导入导出入口           |                               |                                                              |
|                              |                              |                               |                                                              |

> 服务端的职责：1、维护执行器、规则等配置数据，2、收集客户端的函数和变量定义以及表达式执行日志数据。
>
> 后半部分如果不太需要，可以直接让客户端直接连Redis即可，都不需要服务端，前提就是服务端要把数据更新到Redis中。具体参考客户端接入



## 其他

nacos相关版本介绍 : https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E



