package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;

/**
 * Web MVC 示例应用启动入口。
 *
 * <p>演示内容：
 * <ol>
 *   <li>RESTful API 设计（@GetMapping、@PostMapping、@PutMapping、@DeleteMapping）</li>
 *   <li>路径参数提取（@PathVariable）</li>
 *   <li>请求体绑定（@RequestBody）</li>
 *   <li>控制器层、业务层、模型层分层架构</li>
 *   <li>依赖注入（@Inject）</li>
 *   <li>事务管理（@Transactional）- 新增！</li>
 *   <li>事务传播性（REQUIRED、NESTED、REQUIRES_NEW）- 新增！</li>
 *   <li>内嵌 Tomcat 服务器启动</li>
 * </ol>
 *
 * <p>访问地址：http://localhost:8080
 *
 * <p>API 示例：
 * <ul>
 *   <li>GET  /api/orders/1          - 获取订单</li>
 *   <li>POST /api/orders            - 创建订单（带事务）</li>
 *   <li>GET  /api/users/1           - 获取用户</li>
 *   <li>POST /api/users             - 创建用户</li>
 * </ul>
 */
@Slf4j
public class ExampleMain {
    public static void main(String[] args) throws Exception {
        log.info("=== cap-framework Web MVC 示例应用启动 (支持事务模块) ===");
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, args).run()) {
            Thread.currentThread().join();
        }
    }
}

