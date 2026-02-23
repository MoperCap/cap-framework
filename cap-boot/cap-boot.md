# cap-boot 文档

### 1. 自动配置与一站式启动

cap-boot 集成所有 IoC、属性装配、扫描和生命周期能力，极简配置即可运行。

### 2. 注解和扩展

- @Component/@Service/@Repository/@Configuration 自动识别
- @Autowired/@Qualifier/@Value 自动依赖注入与属性注入
- @Subscription/@Subscriber 支持属性动态订阅与回调
- @ComponentScan/@ResourceScan 配置包和资源扫描范围

### 3. 启动流程

1. `new DefaultBootstrapContext(AppConfig.class, args)`
    - 扫描所有注解
    - 属性���件解析，注册 PropertyPublisher
    - 注册所有Initializers，拉起所有 BeanDefinition/订阅等
2. `.build(DefaultApplicationContextFactory.INSTANCE)`  
    - 构造 ApplicationContext，进入运行态
3. `run()` → 启动
4. `close()` → 资源回收

### 4. 属性订阅集成

```java
@Subscription("biz-config")
public class BizManager {
    @Subscriber(propertyKey = "limit.rate", onSet = "onRateChange")
    private Integer rate;

    public void onRateChange(Object newVal) { System.out.println("rate changed: "+newVal); }
}
```

### 5. Initializer与SPI扩展

- 框架提供 InitializerSPI，支持自动发现与执行扩展如AOP/Web/事务等子模块。
- 支持自定义扩展点注册，无需用户手动@Enable等注解。

### 6. FAQ

- 如何只用��核不用自动扫描？
  > 只依赖cap-context+cap-property+cap-bean，完全手动注册，避开 cap-boot。

- 如何接入自定义属性源？
  > 用 PropertyPublisher 注册并发布自己的 PropertySetOperation。
