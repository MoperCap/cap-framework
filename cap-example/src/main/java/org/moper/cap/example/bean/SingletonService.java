package org.moper.cap.example.bean;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.definition.BeanScope;

/**
 * 单例作用域 Bean 示例，演示 {@link BeanScope#SINGLETON} 的行为。
 *
 * <p>每次从容器中获取该 Bean 都会返回同一个实例。
 */
@Slf4j
@Capper(scope = BeanScope.SINGLETON, description = "单例服务示例")
public class SingletonService {

    private static int instanceCount = 0;
    private final int instanceId;

    public SingletonService() {
        instanceId = ++instanceCount;
        log.info("SingletonService 实例 #{} 已创建", instanceId);
    }

    public int getInstanceId() {
        return instanceId;
    }

    public String greet() {
        return "SingletonService#" + instanceId;
    }
}
