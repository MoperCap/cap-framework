package org.moper.cap.example.bean;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;

/**
 * 无接口服务，演示基于 CGLib 的动态代理。
 *
 * <p>该类未实现任何接口，AOP 框架会对其使用 CGLib 子类代理。
 * 切面 {@link org.moper.cap.example.aop.AspectLogger} 将在 {@link #execute(String)}
 * 方法前后执行日志通知。
 */
@Slf4j
@Capper
public class DirectService {

    public String execute(String command) {
        String result = "Executed: " + command;
        log.info("execute called with command={}", command);
        return result;
    }

    public String status() {
        log.info("status called");
        return "DirectService is running";
    }
}
