package org.moper.cap.property.subscriber;

public abstract class AbstractSubscriber implements PropertySubscriber {
    private final String propertyKey;

    public AbstractSubscriber(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * 获取属性订阅者所订阅的属性键 </br>
     *
     * @return 订阅的属性键
     */
    @Override
    public String getPropertyKey() {
        return this.propertyKey;
    }
}
