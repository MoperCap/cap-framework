package org.moper.cap.example.bean;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;

/**
 * 问候服务实现，演示基于接口的 JDK 动态代理。
 *
 * <p>该类实现了 {@link IGreetingService} 接口，AOP 框架会对其使用 JDK Proxy。
 * 切面 {@link org.moper.cap.example.aop.AspectLogger} 将在 {@link #greet(String)}
 * 和 {@link #sendMessage(String)} 方法前后执行日志通知。
 */
@Slf4j
@Capper
public class GreetingServiceImpl implements IGreetingService {

    @Override
    public String greet(String name) {
        String result = "Hello, " + name + "!";
        log.info("greet called with name={}, result={}", name, result);
        return result;
    }

    @Override
    public String sendMessage(String message) {
        String result = "Message sent: " + message;
        log.info("sendMessage called with message={}", message);
        return result;
    }
}
