package org.moper.cap.example.bean;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.definition.BeanScope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 原型作用域 Bean 示例，演示 {@link BeanScope#PROTOTYPE} 的行为。
 *
 * <p>每次从容器中获取该 Bean 都会创建并返回一个新实例。
 */
@Slf4j
@Capper(scope = BeanScope.PROTOTYPE, description = "原型服务示例")
public class PrototypeService {

    private static final AtomicInteger instanceCount = new AtomicInteger(0);

    private final int instanceId;

    public PrototypeService() {
        instanceId = instanceCount.incrementAndGet();
        log.info("PrototypeService 实例 #{} 已创建", instanceId);
    }

    public int getInstanceId() {
        return instanceId;
    }

    public String greet() {
        return "PrototypeService#" + instanceId;
    }
}
