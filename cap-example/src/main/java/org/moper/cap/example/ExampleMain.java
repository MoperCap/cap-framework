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
 *   <li>内嵌 Tomcat 服务器启动</li>
 * </ol>
 *
 * 访问地址：http://localhost:8080
 */
@Slf4j
public class ExampleMain {
    public static void main(String[] args) throws Exception {
        log.info("=== cap-framework Web MVC 示例应用启动 ===");
        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, args).run()) {
            log.info("=== 应用启动完成，按 Ctrl+C 停止 ===");
            Thread.currentThread().join();
        }
    }
}

