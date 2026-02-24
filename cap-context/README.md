# cap-context 文档

### 1. 启动期与运行期上下文

cap-context 统一管理应用生命周期（启动、运行、销毁）与 Bean/属性环境。

### 2. 核心接口

- **BootstrapContext**  
  启动时上下文：已注册所有 BeanDefinition、加载所有属性、执行所有 Initializer。
- **ApplicationContext**  
  运行期上下文：用于Bean/属性的获取、生命周期管理等。
- **Environment**  
  运行期属性环境，集成 PropertyOfficer + PropertyViewPool。

### 3. 用法示例

```java
try(ApplicationContext ctx =
      new DefaultBootstrapContext(AppConfig.class, args).build(DefaultApplicationContextFactory.INSTANCE))
{
    ctx.run();
    MyService service = ctx.getBean(MyService.class);
    String prop = ctx.getEnvironment().getViewPool().getPropertyValue("my.prop", String.class);
}
```

### 4. 生命周期

- `run()` → 实例化所有Bean，注册关闭钩子
- `close()` → 释放所有Bean与属性订阅、断开Publisher、关停Officer

### 5. Environment高级能力

- registerPublisher/unregisterPublisher
- 属性事件全自动通知

### 6. FAQ

- BootstrapContext 和 ApplicationContext 如何分工？  
  > BootstrapContext 负责初始化，ApplicationContext 负责生命周期与运行期管理。