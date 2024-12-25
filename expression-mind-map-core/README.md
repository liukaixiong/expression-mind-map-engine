# 通用的函数执行器

该工具是基于模版引擎抽象出来的通用定义，默认是基于Aviator实现，集成Spring拓展而来。该工具可以针对复杂业务进行分层、将业务模型抽象定义成变量、函数，然后通过表达式进行组装执行。

该工具实现的功能如下:

- 变量注册
    - 全局变量
    - 分组变量
- 函数注册
    - 全局函数
- 表达式调试、检验、逻辑运算
- 表达式文档定义
- 支持远端变量、函数运算

待实现功能:

- [ ] 流程日志

- [ ] 行为埋点

- [ ] 告警能力

- [ ] 数据统计

## 如何使用?

引入依赖

```xml

<dependency>
    <groupId>com.saicmotor.common.engine</groupId>
    <artifactId>smp-engine-core</artifactId>
    <version>${project.version}</version>
</dependency>
```

使用表达式

### 简单使用

```java

@Autowired
private ExpressionExecutorFactory expressionManager;

@Test
public void simpleCase() {
    Map<String, Object> user = new HashMap<>();
    user.put("groupName", "liukaixiong");
    user.put("id", 123);
    user.put("age", 16);

    ExpressionService expressionService = expressionManager.getExpressionService();
    // 所有入参要被引用前缀新增:req
    Object execute = expressionService.execute("req.id", user);
    System.out.println(execute);

    Object execute1 = expressionService.execute("req.age", user);
    System.out.println(execute1);


    Object execute2 = expressionService.execute("fn.test('a',1)", user);
    System.out.println(execute2);
}
```

### 实现变量组注册

按照组来区分变量，相互隔离。

这个代表的是默认的组.请实现`AbstractExpressionVariableProcess`

```java
public class ExpressionVariableProcessAdaptor extends AbstractExpressionVariableDefinition {

    @Override
    public String groupName() {
        return ExpressionTypeEnums.DEFAULT.name();
    }

}
```

特定的组变量

```java

@Component
public class ConditionExpressionProcess extends AbstractExpressionVariableDefinition<Object> {

    @Override
    public String groupName() {
        return "condition";
    }

    @VariableKey(name = "qiandao", describe = "今天是否签到")
    private boolean qiandao(ContextTemplateRequest cache) {
        System.out.println("触发qiandao");
        return true;
    }

    @VariableKey(name = "lianxuqiandao", describe = "连续签到")
    private boolean lianxuqiandao(ContextTemplateRequest cache) {
        return true;
    }

    @VariableKey(name = "qiandaozongshu", describe = "签到总数")
    private Integer qiandaoCount(ContextTemplateRequest cache) {
        return 10;
    }

    @VariableKey(name = "isVip", describe = "用户是否Vip")
    private boolean isVip(ContextTemplateRequest cache) {
        System.out.println("触发isVip");
        return false;
    }

    @VariableKey(name = "username", describe = "")
    private String username(ContextTemplateRequest cache) {
        return "liukaixiong";
    }

}
```

这个时候我需要引用condtion组的变量

```java

@Autowired
private ExpressionExecutorFactory expressionManager;


// 默认的组 
ExpressionService expressionService = expressionManager.getExpressionService();

// 特定的组
ExpressionService conditionExpression = expressionManager.getExpressionService("condition");
Boolean conditionResult = (Boolean) conditionExpression.execute("qiandao == true && isVip == false", requestData);


```

**组与组之间的变量是可以相互隔离的，避免冲突。**

### 实现全局变量注册

只需要实现`ExpressionVariableRegister` 接口

```java

@Component
public class CommonVariableRegister implements ExpressionVariableRegister, InitializingBean {

    private Map<String, Object> dbCache = new HashMap<>();

    @Override
    public boolean finderVariable(String name) {
        return dbCache.containsKey(name);
    }

    @Override
    public Object invoke(String name, ContextTemplateRequest cache) throws Exception {
        return dbCache.get(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dbCache.put("project", "spring-boot-expression");
        dbCache.put("env", "test");
        dbCache.put("version", 1);
        dbCache.put("isEnableCache", true);
    }
}
```

调试

```java
ExpressionService expressionService = expressionManager.getExpressionService();

Object execute = expressionService.execute("project", user);
System.out.

println(execute);

Object execute1 = expressionService.execute("env", user);
System.out.

println(execute1);
// spring-boot-expression
// test
```

**全局变量是组与组之间都共享的。**

### 实现函数注册

实现`AbstractExpressionFunctionProcess` 接口

```java

@Component
public class FunctionExpressionProcess extends AbstractExpressionFunctionProcess {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String functionPrefix() {
        return "fn";
    }

    @FunctionKey(name = "test", describe = "测试1", requestClass = UserModel.class)
    public String test(ContextTemplateRequest request, Map<String, Object> reqMap) {
        logger.info("触发函数:test1 , 请求参数:{} , 变量参数:{} ", reqMap, JSONUtil.toJsonStr(request));
        return "OK";
    }

    @FunctionKey(name = "test2", describe = "测试2", requestClass = UserModel.class)
    public void test2(ContextTemplateRequest request, Map<String, Object> reqMap) {
        logger.info("触发函数:test2 , 请求参数:{} , 变量参数:{} ", reqMap, JSONUtil.toJsonStr(request));
    }
}
```

这里默认的入参都是Map的方式

```java
Object execute2 = expressionService.execute("fn.test('a',1)", user);
```

#### 函数的参数描述

如果你的参数描述是

```java
public class UserModel {
    @PropertyDefinition(value = "用户名称")
    private String name;
    @PropertyDefinition(value = "年龄")
    private int age;
    @PropertyDefinition(value = "主键", required = true)
    private Integer id;
}

```

## 新版本函数使用

如果你需要实现一个函数,那么请实现:`AbstractSimpleFunction`
函数相关的描述可以参考:`DemoFunDescDefinitionService` 








