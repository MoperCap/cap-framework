# cap-aop 模块说明

cap-aop 为 CAP Framework 独立扩展模块，提供基本轻量级 AOP 能力（面向接口 Bean，支持 Before/Around/After 通知），自动集成到 IoC 启动流程。

## 1. 快速使用

1. Maven/Gradle 加入 cap-aop 模块依赖
2. 在你的切面类上加 @Aspect，并在方法上加 @Before/@Around/@After 注解，表达式示例如 `com.example.MyService.doWork`
3. 切面方法参数与目标方法参数一致（支持零参数）；切点支持 “类名.方法名” 精确匹配。

## 2. 代码示例

```java
@Aspect
public class LoggingAspect {
    @Before("com.example.MyService.doWork")
    public void beforeWork() {
        System.out.println("before doWork");
    }

    @After("com.example.MyService.doWork")
    public void afterWork() {
        System.out.println("after doWork");
    }
}
```

## 3. 集成到 cap-boot

cap-aop 会自动被 cap-boot SPI 扫描注册，不会强依赖主模块。  
未引入 cap-aop 时，AOP功能不会生效。  
添加 `org.moper.cap.aop.runner.AopBootstrapRunner` 到 SPI 配置即可。

## 4. 通知类型说明

- @Before: 方法执行前调用
- @Around: 替代原方法（只执行通知方法，不执行目标方法）
- @After: 方法执行后调用

## 5. 局限

- 当前只支持接口类的 Bean（JDK Proxy）
- 切点表达式仅支持 “类名.方法名” 精确匹配
- 后续可扩展正则/参数对象/异常通知等
