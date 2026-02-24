package org.moper.cap.property.subscriber;

/**
 * 单一属性订阅者 </br>
 */
public interface PropertySubscriber {

    String getPropertyKey();

    void onSet(Object value);

    void onRemoved();
}
