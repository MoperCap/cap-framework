package org.moper.cap.example.bean;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.property.annotation.Subscriber;
import org.moper.cap.property.annotation.Subscription;

/**
 * 属性监听示例，演示 {@link Subscription} 和 {@link Subscriber} 注解的使用。
 *
 * <p>标注 {@link Subscription} 表示该 Bean 是一个属性订阅客户端。
 * 内部通过 {@link Subscriber} 字段订阅特定属性键的变更事件，
 * 属性变更时通过回调方法获取最新值，属性被移除时通过另一回调方法处理。
 */
@Slf4j
@Getter
@Capper
@Subscription("dynamicConfig")
public class DynamicConfigService {

    @Subscriber(propertyKey = "dynamic.debug", onSet = "onDebugChanged", onRemoved = "onDebugRemoved")
    private Boolean debugEnabled;

    @Subscriber(propertyKey = "dynamic.cache.ttl", onSet = "onCacheTtlChanged")
    private Integer cacheTtl;

    private boolean debugRemovedCalled = false;

    void onDebugChanged(Boolean newVal) {
        this.debugEnabled = newVal;
        log.info("dynamic.debug changed to: {}", newVal);
    }

    void onDebugRemoved() {
        this.debugRemovedCalled = true;
        log.info("dynamic.debug property removed");
    }

    void onCacheTtlChanged(Integer newVal) {
        this.cacheTtl = newVal;
        log.info("dynamic.cache.ttl changed to: {}", newVal);
    }
}
