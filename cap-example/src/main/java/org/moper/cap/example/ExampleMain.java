package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;

/**
 * CAP Framework 示例应用启动入口。
 *
 * <p>演示内容：
 * <ol>
 *   <li>Web MVC 框架 - RESTful API</li>
 *   <li>依赖注入 (DI) - @Inject 注解</li>
 *   <li>事务管理 - @Transactional 注解</li>
 *   <li>数据库访问 - MySQL 连接</li>
 *   <li>日志记录 - SLF4J + Logback</li>
 *   <li>属性管理 - application.yaml 配置</li>
 *   <li>内嵌 Tomcat 服务器</li>
 * </ol>
 *
 * <p>API 访问地址：http://localhost:8080
 *
 * <p>示例 API：
 * <ul>
 *   <li>GET  /api/users           - 获取所有用户</li>
 *   <li>GET  /api/users/1         - 获取指定用户</li>
 *   <li>POST /api/users           - 创建用户</li>
 *   <li>GET  /api/products        - 获取商品列表</li>
 *   <li>GET  /api/orders          - 获取订单列表</li>
 *   <li>POST /api/orders          - 创建订单（带事务）</li>
 * </ul>
 *
 * <p>启动流程：
 * <ol>
 *   <li>加载 application.yaml 配置</li>
 *   <li>扫描 org.moper.cap.example 包下的组件</li>
 *   <li>初始化数据源 (MySQL)</li>
 *   <li>注册事务拦截器</li>
 *   <li>启动 Web MVC 框架</li>
 *   <li>启动内嵌 Tomcat 服务器</li>
 * </ol>
 *
 * @author CAP Framework Team
 * @version 1.0.0
 */
@Slf4j
public class ExampleMain {

    public static void main(String[] args) throws Exception {
        log.info("╔════════════════════════════════════════╗");
        log.info("║  CAP Framework 示例应用启动              ║");
        log.info("║  Version: 1.0.0                         ║");
        log.info("╚════════════════════════════════════════╝");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, args).run()) {
            log.info("╔════════════════════════════════════════╗");
            log.info("║  应用启动完成！                          ║");
            log.info("║  访问地址: http://localhost:8080       ║");
            log.info("║  按 Ctrl+C 停止应用                     ║");
            log.info("╚════════════════════════════════════════╝");

            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("应用启动失败", e);
            System.exit(1);
        }
    }
}

