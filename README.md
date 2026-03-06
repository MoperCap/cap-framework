# Cap Framework

基于 Java 21 实现的面向 Web 开发的高性能 MVC 架构框架

## 功能概述

Cap Framework 是一个轻量级 Web MVC 框架，支持依赖注入、事务管理、属性配置、内嵌 Tomcat、RESTful API、AOP拦截器等。

它专为 Java Web 应用设计，提供类似 Spring Boot 的开发体验，聚焦于规范性、易用性与可扩展性。

## 主要特性

- **Web MVC 架构**：支持 RESTful 路由、请求调度、视图渲染
- **依赖注入 (DI)**：基于 `@Capper` 注解的自动 Bean 注册与注入
- **事务管理**：快速集成数据库事务控制
- **属性管理**：通过 `application.yaml`, `@Value` 注解实现配置注入
- **内嵌服务器**：直接启动支持 Tomcat
- **组件扫描**：自动扫描项目包下所有组件
- **AOP 扩展**：支持拦截器链
- **生命周期管理**：标准 Bean 生命周期规范

## 安装与配置

### 1. 环境要求

- JDK 21
- Maven
- MySQL（如需数据库功能）

### 2. 如何集成到你的项目

用 Maven 添加依赖（示例）：

XML

```
<dependency>
    <groupId>org.moper.cap</groupId>
    <artifactId>cap-framework</artifactId>
    <version>最新版本</version>
</dependency>
```