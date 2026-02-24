package org.moper.cap.property.subscriber;

/**
 * 单一属性订阅者 </br>
 *
 * @param <T> 所监听的属性类型
 */
public interface PropertySubscriber<T> {

    String getPropertyKey();

    void onSet(T value);

    void onRemoved();
}
