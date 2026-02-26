package org.moper.cap.property.subscriber;

/**
 * 单一属性订阅者 </br>
 *
 * @param <T> 所监听的属性类型
 */
public interface PropertySubscriber<T> {

    PropertySelector selector();

    Class<?> getSubscribeType();

    void onSet(T value);

    void onRemoved();
}
