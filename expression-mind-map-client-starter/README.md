# 通用的函数执行器

该工具是基于模版引擎抽象出来的通用定义，默认是基于Aviator实现，集成Spring拓展而来。该工具可以针对复杂业务进行分层、将业务模型抽象定义成变量、函数，然后通过表达式进行组装执行。

> 本质上所有表达式都存储在服务端进行管理，客户端负责从服务端进行获取规则表达式，在客户端进行本地执行，所以不用担心性能，本质上还是业务表达式的逻辑会成为瓶颈。

## 如何使用?

引入依赖

```xml
 <dependency>
     <groupId>com.liukx.expression.engine</groupId>
     <artifactId>expression-mind-map-client-starter</artifactId>
     <version>${project.version}</version>
</dependency>
```

### 配置介绍

```yaml
spring:
  plugin:
    express:
      debug: true
      remote-engine-url: http://127.0.0.1:20876  # 可直接指定服务端地址，不走注册中心查找
      logger-trace-level: debug 				 # 默认的日志级别配置
      expression-config-call: http				 # 获取表达式配置内容的方式： http、redis ,如果服务端和客户端都处于一个redis库的话,可以考虑配置redis,减少http访问
      enable-trace-log: true					 # 是否打开追踪日志传输，关闭的话，则无法使用追踪能力
      inject-type-package: com.smp.service.function.control # 注入类型包路径，该属性作用是在搜索表达式的时候，指定entity会检索到对应的属性
```

## 1、使用表达式

```java
// 注入依赖
@Resource
private ClientEngineFactory clientEngineFactory;

// 定义上下文对象
final ExpressionEnvContext expressionEnvContext = new ExpressionEnvContext();
expressionEnvContext.setEventName(eventCode);
// 将业务类注入到上下文中,方便函数或者变量获取
expressionEnvContext.addEnvClassInfo(activityConfig);
expressionEnvContext.addEnvClassInfo(shareJoinLog);
expressionEnvContext.addEnvClassInfo(userJoinInfo);
expressionEnvContext.addEnvClassInfo(activityEventModel);
// 注入自定义变量，可在表达式中进行运用
expressionEnvContext.addEnvContext("eventCode", eventCode);

// 业务参数
ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
request.setBusinessCode("activity"); 	// 业务组
request.setExecutorCode(activityCode);  // 执行码
request.setEventName(eventCode);		// 事件码
request.setUserId(userId);				// 用户编号
request.setUnionId(userId + "");		// 业务编号
request.setTraceId(TraceHelper.getTraceId()); // 追踪编号
request.setRequest(activityEventModel);		 // 请求参数
// 执行表达式内容
final Boolean invoke = clientEngineFactory.invoke(request, expressionEnvContext);
```

- BusinessCode: 业务组，某个业务模块
- ExecutorCode: 执行码，可以理解为业务功能
- EventName: 事件码，当前执行的事件

以活动举例:

- BusinessCode: 活动模块
- ExecutorCode: 活动编码
- EventName: 活动内部事件 // 非必须

## 2、自定义函数

简单的函数实现`AbstractSimpleFunction` 即可

```java
public class DemoSendPointFunction extends AbstractSimpleFunction {

   
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        /*
            以下是一些常用的工具类案例使用方式
         */
        // 获取第一个参数，如果没有获取到则会抛出异常。适用于必填参数是否填写.
        Object firstArgs = getArgsIndexValue(funArgs, 0);

        // 获取第二个参数，如果没有的话，则返回默认值，适用于兼容老的函数
        Long argsIndexValue = getArgsIndexValue(funArgs, 1, 100L);

        // 从上下文变量中获取上游注入的对象：FunctionRequestDocumentModel
        // 适用于针对特定的业务模型函数获取该业务的上下文参数对象
        FunctionRequestDocumentModel documentModel = env.getObject(FunctionRequestDocumentModel.class);

        // 可以加入追踪页面可查找的日志信息
        // getName() 函数名称 ,  key : 标识  value : 日志内容
        env.recordTraceDebugContent(getName(),"key","内容");
        return null;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        // 函数名称定义
        return DemoFunDescDefinitionService.SEND_POINT;
    }

}
```

函数相关的定义，涵盖`名称` 、`参数` 、`描述` 可通过`ExpressFunctionDocumentLoader` 定义，建议使用枚举类统一规范起来。

```java
public enum DemoFunDescDefinitionService implements ExpressFunctionDocumentLoader {

    SEND_POINT("activity", "send_point", "赠送积分", new String[]{"积分编码", "积分值"}, "true || false", "send_point('aa','50')");

    private FunctionApiModel functionApiModel;

    DemoFunDescDefinitionService(String group, String code, String describe, String[] requestDesc, String returnDescribe, String example) {
        FunctionApiModel def = new FunctionApiModel();
        def.setName(code);
        def.setGroupName(group);
        def.setDescribe(describe);
        def.setResultClassType(returnDescribe);
        def.setArgs(Arrays.asList(requestDesc));
        def.setExample(example);
        this.functionApiModel = def;
    }
    
    @Override
    public FunctionApiModel loadFunctionInfo() {
        return this.functionApiModel;
    }
}
```

#### 函数的参数描述

1. 建立函数枚举列表

```java
/**
 * 基础使用方式
 *
 * @author liukaixiong
 * @date 2023/12/7
 */
public enum DemoFunDescDefinitionService implements ExpressFunctionDocumentLoader {

    SEND_POINT("activity", "send_point", "赠送积分", new String[]{"积分编码", "积分值"}, "true || false", "send_point('aa','50')");

    private FunctionApiModel functionApiModel;

    DemoFunDescDefinitionService(String group, String code, String describe, String[] requestDesc, String returnDescribe, String example) {
        FunctionApiModel def = new FunctionApiModel();
        def.setName(code);
        def.setGroupName(group);
        def.setDescribe(describe);
        def.setResultClassType(returnDescribe);
        def.setArgs(Arrays.asList(requestDesc));
        def.setExample(example);
        this.functionApiModel = def;
    }

    @Override
    public FunctionApiModel loadFunctionInfo() {
        return this.functionApiModel;
    }
}
```

2. 实现函数

```java
/**
 * 发送积分案例
 *
 * @author liukaixiong
 * @date 2023/12/7
 */
public class DemoSendPointFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(Map<String, Object> env, ExpressionBaseRequest request, List<Object> funArgs) {
        /*
            以下是一些常用的工具类案例使用方式
         */
        // 获取第一个参数，如果没有获取到则会抛出异常。适用于必填参数是否填写.
        Object firstArgs = getArgsIndexValue(funArgs, 0);

        // 获取第二个参数，如果没有的话，则返回默认值，适用于兼容老的函数
        Long argsIndexValue = getArgsIndexValue(funArgs, 1, 100L);

        // 从上下文变量中获取上游注入的对象：FunctionRequestDocumentModel
        // 适用于针对特定的业务模型函数获取该业务的上下文参数对象
        FunctionRequestDocumentModel documentModel = ExpressionContextHelper.getObject(env, FunctionRequestDocumentModel.class);
        return null;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return DemoFunDescDefinitionService.SEND_POINT;
    }

}
```

## 通用函数列表

| 函数类型    | 函数名称                                             | 函数作用                          |
|---------|--------------------------------------------------|-------------------------------|
| 流程分支控制  | fn_end()                                         | 满足当前分支，能继续进入子分支，不在进入往后的同级别分支。 |
| 流程分支控制  | fn_force_end()                                   | 满足当前节点，则直接结束流程，不在往下执行         |
| 调试函数    | debug_body()                                     | 追踪链路中打印请求参数体                  |
| 调试函数    | debug_object()                                   | 追踪链路页面中：打印指定的参数对象             |
| 条件-时间类型 | fn_sys_date_hour_range(9,20)                     | 是否在小时时间范围处理(基于系统时间)           |
| 条件-时间类型 | fn_sys_date_day_range('2024-08-21','2024-08-25') | 是否在当前日期范围内                    |
| 上下文设置   | fn_record_result_context('result','abc')         | 手动设置变量到上下文中                   |

> 你可以根据自己的想法制定想要的能力，在表达式中注入即可。通用实现类在: `com.liukx.expression.engine.client.function`中

## 3、自定义变量 ExpressionVariableRegister

变量可以分为静态和动态两种方式:

- 静态变量是直接在调用的时候，将值设置到上下文中，比如： `ExpressionEnvContext`
    - `expressionEnvContext.addEnvContext("eventCode", eventCode)`
- 动态变量：在表达式中指定，表达式解析完成之后会调用`ExpressionVariableRegister` 进行查找匹配，找到之后会放入上下文中。
    - 来源可以从数据库、内调调用、第三方调用，根据自己的业务逻辑实现即可。

通过实现`ExpressionVariableRegister`接口即可。建议变量的管理和枚举进行绑定，比如：

### 3.1 抽象业务变量

```java
public abstract class AbstractExpressionVariableContext implements ExpressionVariableRegister {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean finderVariable(String name) {
        return variableName().name().equals(name);
    }

    public abstract BaseVariableEnums variableName();

    @Override
    public Object invoke(String name, ContextTemplateRequest cache) {
        final Object processor = processor(name, cache.getRequest(), cache.getEnvContext()); 
        return processor;
    }

    @Override
    public List<VariableApiModel> loadVariableList() {
        final BaseVariableEnums baseVariableEnums = variableName();
        VariableApiModel variables = new VariableApiModel();
        variables.setName(baseVariableEnums.name());
        variables.setDescribe(baseVariableEnums.getDesc());
        variables.setGroupName(groupName());
        variables.setType(baseVariableEnums.getReturnType().getSimpleName());
        return Collections.singletonList(variables);
    }

    @Override
    public VariableApiModel getVariableInfo(String group, String name) {
        return null;
    }

    @Override
    public String groupName() {
        return "var";
    }

    public abstract Object processor(String name, ExpressionBaseRequest request, Map<String, Object> envContext);
}
```

枚举管理: 将所有变量维护到该类中，包括变量定义、描述，出参类型。可方便后续检索

```java
@Getter
public enum BaseVariableEnums {
    env_family_valid_order_obj("家庭首个有效订单信息", DshCustomerOrder.class),
    env_family_valid_order_obj_v2("家庭首个有效订单信息(涵盖研学)", DshCustomerOrder.class),
    env_yqr_family_valid_order_obj("邀请人家庭首个有效订单信息", DshCustomerOrder.class),
    env_yqr_family_valid_order_obj_v2("邀请人家庭首个有效订单信息(涵盖研学)", DshCustomerOrder.class),
    env_user_obj("用户信息", DshStudent.class),
    env_user_yqr_obj("邀请人用户信息", DshStudent.class);

    // 变量描述
    private final String desc;
    // 出参类型, 直接指定对象即可.
    // 变量通过: spring.plugin.express.inject-type-package 扫描之后,可以在表达式配置页面进行索引
    private final Class<?> returnType;

    BaseVariableEnums(String desc, Class<?> returnType) {
        this.desc = desc;
        this.returnType = returnType;
    }
}
```

```java
@Component
public class UserEnvVariableRegister extends AbstractExpressionVariableContext {

    @Resource
    private IXService service;

    @Override
    public BaseVariableEnums variableName() {
        return BaseVariableEnums.env_user_obj;
    }

    @Override
    public Object processor(String name, ExpressionBaseRequest request, Map<String, Object> envContext) {
        final Long userId = getUser(name, request, envContext);
        return service.getById(userId);
    }

    protected Long getUser(String name, ExpressionBaseRequest request, Map<String, Object> envContext) {
        return request.getUserId();
    }

}
```

### 3.2 数据库变量

可自定义实现即可

```java
// 注入bean
public class DemoEnvRegister implements ExpressionVariableRegister {

    @Override
    public List<VariableApiModel> loadVariableList() {
        // 如果有多个变量,则返回多个
        return null;
    }

    @Override
    public VariableApiModel getVariableInfo(String group, String name) {
        // 定义变量信息
        return null;
    }

    @Override
    public boolean finderVariable(String name) {
        // 查询数据库是否存在
        return false;
    }

    @Override
    public Object invoke(String name, ContextTemplateRequest cache) {
        // 处理业务逻辑
        return null;
    }

    @Override
    public String groupName() {
        return "db";
    }
}
```

## 4. 表达式生命周期

![image-20241224164743320](../doc/images/image-20241224164743320.png)

### 4.1 执行器拦截 ExpressionExecutorPostProcessor

在获取到规则之后，可执行beforeExecutor、afterExecutor回调

### 4.2 表达式拦截 : ExpressionConfigExecutorIntercept

执行到每条表达式的时候，可实现进行拦截处理

### 4.3 函数拦截: ExpressionFunctionPostProcessor

在执行到每个函数的时候，前、后、异常都可拦截

### 4.4 函数责任链 : ExpressionFunctionFilter

函数执行前后拦截，可强制变更结果。
