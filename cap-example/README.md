# cap-example 项目集成演示

本模块演示 CAP Framework 的全部核心能力：

- Bean定义与依赖注入（cap-bean）
- 属性系统事件驱动/订阅（cap-property）
- 启动/环境/生命周期管理（cap-context）
- 自动装配/注解扫描（cap-boot）
- 切面增强与AOP（cap-aop）

## 启动方法

```shell
./mvnw compile exec:java -Dexec.mainClass=org.moper.cap.example.Main
```

## 典型用法

参见 Main.java 和 ExampleIntegrationTest.java
