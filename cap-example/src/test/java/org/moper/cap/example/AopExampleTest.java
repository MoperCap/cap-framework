package org.moper.cap.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.moper.cap.boot.application.impl.DefaultCapApplication;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.example.aop.AspectLogger;
import org.moper.cap.example.bean.DirectService;
import org.moper.cap.example.bean.IGreetingService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AOP 动态代理集成测试
 *
 * <p>测试内容：
 * <ol>
 *   <li>JDK Proxy 测试：通过 {@link IGreetingService} 接口获取代理 Bean，验证切面计数器递增</li>
 *   <li>CGLib Proxy 测试：通过 {@link DirectService} 获取代理 Bean，验证切面计数器递增</li>
 *   <li>验证代理后方法的返回值仍然正确</li>
 * </ol>
 */
@Slf4j
public class AopExampleTest {

    /**
     * 测试：JDK 动态代理 — 调用接口方法后，AspectLogger 的 beforeCount/afterCount 正确递增
     */
    @Test
    void testJdkProxyBeforeAndAfterAdvice() throws Exception {
        log.info("\n========== 测试：JDK 动态代理 (IGreetingService) ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            IGreetingService greetingService = context.getBean("greetingServiceImpl", IGreetingService.class);
            assertNotNull(greetingService, "greetingServiceImpl Bean 不应为空");

            AspectLogger aspectLogger = context.getBean("aspectLogger", AspectLogger.class);
            assertNotNull(aspectLogger, "aspectLogger Bean 不应为空");

            int beforeBefore = aspectLogger.getBeforeCount();
            int afterBefore = aspectLogger.getAfterCount();

            // 调用 greet() — 触发 @Before 和 @After
            String greetResult = greetingService.greet("World");
            assertEquals("Hello, World!", greetResult, "greet() 返回值应正确");
            assertEquals(beforeBefore + 1, aspectLogger.getBeforeCount(), "调用 greet() 后 beforeCount 应加 1");
            assertEquals(afterBefore + 1, aspectLogger.getAfterCount(), "调用 greet() 后 afterCount 应加 1");

            // 调用 sendMessage() — 再次触发 @Before 和 @After
            String msgResult = greetingService.sendMessage("Hello CAP");
            assertTrue(msgResult.contains("Hello CAP"), "sendMessage() 返回值应包含原消息");
            assertEquals(beforeBefore + 2, aspectLogger.getBeforeCount(), "调用 sendMessage() 后 beforeCount 应再加 1");
            assertEquals(afterBefore + 2, aspectLogger.getAfterCount(), "调用 sendMessage() 后 afterCount 应再加 1");

            log.info("✅ JDK 动态代理成功：beforeCount={}, afterCount={}", aspectLogger.getBeforeCount(), aspectLogger.getAfterCount());
        }
    }

    /**
     * 测试：CGLib 动态代理 — 调用无接口类的方法后，AspectLogger 的 beforeCount/afterCount 正确递增
     */
    @Test
    void testCglibProxyBeforeAndAfterAdvice() throws Exception {
        log.info("\n========== 测试：CGLib 动态代理 (DirectService) ==========\n");

        try (RuntimeContext context = new DefaultCapApplication(ExampleApplication.class, "--server.port=0").run()) {
            DirectService directService = context.getBean("directService", DirectService.class);
            assertNotNull(directService, "directService Bean 不应为空");

            AspectLogger aspectLogger = context.getBean("aspectLogger", AspectLogger.class);
            assertNotNull(aspectLogger, "aspectLogger Bean 不应为空");

            int beforeBefore = aspectLogger.getBeforeCount();
            int afterBefore = aspectLogger.getAfterCount();

            // 调用 execute() — 触发 @Before 和 @After
            String result = directService.execute("test-command");
            assertTrue(result.contains("test-command"), "execute() 返回值应包含命令内容");
            assertEquals(beforeBefore + 1, aspectLogger.getBeforeCount(), "调用 execute() 后 beforeCount 应加 1");
            assertEquals(afterBefore + 1, aspectLogger.getAfterCount(), "调用 execute() 后 afterCount 应加 1");

            log.info("✅ CGLib 动态代理成功：beforeCount={}, afterCount={}", aspectLogger.getBeforeCount(), aspectLogger.getAfterCount());
        }
    }
}
