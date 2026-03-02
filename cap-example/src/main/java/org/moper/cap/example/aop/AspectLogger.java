package org.moper.cap.example.aop;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.aop.annotation.After;
import org.moper.cap.aop.annotation.Aspect;
import org.moper.cap.aop.annotation.Before;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.example.bean.DirectService;
import org.moper.cap.example.bean.IGreetingService;

/**
 * AOP 切面示例，演示 {@link Aspect}、{@link Before} 和 {@link After} 注解的使用。
 *
 * <ul>
 *   <li>{@link Before} — 方法执行前记录日志（前置通知）</li>
 *   <li>{@link After} — 方法执行后记录日志（后置通知）</li>
 * </ul>
 *
 * <p>切点格式：{@code 完全限定类名.方法名}。
 * <ul>
 *   <li>JDK Proxy 时，切点使用接口的类名（{@link IGreetingService}）。</li>
 *   <li>CGLib Proxy 时，切点使用目标类的类名（{@link DirectService}）。</li>
 * </ul>
 */
@Slf4j
@Getter
@Capper
@Aspect
public class AspectLogger {

    private int beforeCount = 0;
    private int afterCount = 0;

    // ─── JDK Proxy 切点（IGreetingService 接口方法） ───

    @Before("org.moper.cap.example.bean.IGreetingService.greet")
    public void beforeGreet() {
        beforeCount++;
        log.info("[Before] IGreetingService.greet — beforeCount={}", beforeCount);
    }

    @After("org.moper.cap.example.bean.IGreetingService.greet")
    public void afterGreet() {
        afterCount++;
        log.info("[After] IGreetingService.greet — afterCount={}", afterCount);
    }

    @Before("org.moper.cap.example.bean.IGreetingService.sendMessage")
    public void beforeSendMessage() {
        beforeCount++;
        log.info("[Before] IGreetingService.sendMessage — beforeCount={}", beforeCount);
    }

    @After("org.moper.cap.example.bean.IGreetingService.sendMessage")
    public void afterSendMessage() {
        afterCount++;
        log.info("[After] IGreetingService.sendMessage — afterCount={}", afterCount);
    }

    // ─── CGLib Proxy 切点（DirectService 类方法） ───

    @Before("org.moper.cap.example.bean.DirectService.execute")
    public void beforeExecute() {
        beforeCount++;
        log.info("[Before] DirectService.execute — beforeCount={}", beforeCount);
    }

    @After("org.moper.cap.example.bean.DirectService.execute")
    public void afterExecute() {
        afterCount++;
        log.info("[After] DirectService.execute — afterCount={}", afterCount);
    }
}
