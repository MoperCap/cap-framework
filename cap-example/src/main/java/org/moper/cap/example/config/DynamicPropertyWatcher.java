package org.moper.cap.example.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.property.annotation.Subscriber;
import org.moper.cap.property.annotation.Subscription;

/**
 * 动态属性监听示例 Bean，演示 {@link Subscription} 和 {@link Subscriber} 注解的使用。
 *
 * <p>通过 {@link org.moper.cap.property.publisher.PropertyPublisher} 发布属性变更时，
 * 对应的 {@code onSet} / {@code onRemoved} 回调方法将被自动触发。
 */
@Slf4j
@Getter
@Capper
@Subscription("dynamicPropertyWatcher")
public class DynamicPropertyWatcher {

    @Subscriber(propertyKey = "app.dynamic.value", onSet = "onDynamicValueSet", onRemoved = "onDynamicValueRemoved")
    private String dynamicValue;

    private String lastSetValue;
    private boolean removedCalled;

    void onDynamicValueSet(String newVal) {
        this.dynamicValue = newVal;
        this.lastSetValue = newVal;
        log.info("DynamicPropertyWatcher.onDynamicValueSet: {}", newVal);
    }

    void onDynamicValueRemoved() {
        this.removedCalled = true;
        log.info("DynamicPropertyWatcher.onDynamicValueRemoved");
    }
}
