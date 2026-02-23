# cap-bean 文档

### 1. IoC容器设计

cap-bean 提供极致轻量、高性能的**Java IoC容器**，支持手动及自动 Bean 注册、依赖注入、生命周期管理和自定义扩展。

### 2. 核心接口

- **BeanContainer**
  - 注册、获取、销毁 Bean。
- **BeanDefinition**
  - Bean 的定义描述，支持作用域、惰性、构造器参数配置等。
- **BeanProvider/BeanInspector**
  - 运行期Bean查询/元数据检查。
- **BeanInterceptor**
  - Bean创建/注入/销毁阶段的扩展点。

### 3. 常用能力

- 支持 `BeanDefinition.of(...)` 注册外部Bean。
- 支持单例/原型 BeanScope，懒加载、@Primary 选项, BeanInterceptor 插拔。
- 手动获取注册的任意Bean、支持类型和名称两种模式。
- 销毁/释放资源调用生命周期钩子。

```java
BeanContainer container = new DefaultBeanContainer();
container.registerBeanDefinition(BeanDefinition.of("demoService", DemoService.class));
DemoService srv = container.getBean("demoService", DemoService.class);
```

### 4. 拦截器与拓展

支持 BeanInterceptor 自动拦截初始化、属性注入、销毁等流。

### 5. FAQ

- 如何只用IoC内核不用注解？
  > 你可以全部用BeanDefinition+手动注册，全流程不依赖扫描与注解。
