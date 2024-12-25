# 规则引擎服务端

## 源码启动

1. 编译项目

`maven install `

2. 初始化表结构SQL : `script/expression_mysql.sql`
3. 配置文件

- 非nacos的话需要指定Mysql和Redis的地址，可以在启动参数中添加 engine.xxx部分
- nacos则参考`script/expression-mind-map-server.yml` ，参考`bootstrap.yml`

4. 启动类: `BootApplication`

## 其他

nacos相关版本介绍 : https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E



