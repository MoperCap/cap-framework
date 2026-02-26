package org.moper.cap.property.subscriber;

/**
 * 属性订阅者 </br>
 *
 * @param <T> 所监听的属性类型
 */
public interface PropertySubscriber<T> {

    /**
     * 获取当前属性订阅者有关属性键的属性选择器
     *
     * @return 属性选择器
     */
    PropertySelector selector();

    /**
     * 获取当前属性订阅者所监听的属性类型
     *
     * @return 监听的属性类型
     */
    Class<T> getSubscribeType();

    /**
     * 当前属性订阅者所监听的属性值发生变化时的回调接口
     *
     * @param value 最新的属性值
     */
    void onSet(T value);

    /**
     * 当前属性订阅者所监听的属性值被移除时的回调接口
     */
    void onRemoved();
}
