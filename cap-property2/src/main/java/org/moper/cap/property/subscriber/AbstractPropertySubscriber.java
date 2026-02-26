package org.moper.cap.property.subscriber;

import org.moper.cap.property.subscriber.selector.SinglePropertySelector;

public abstract class AbstractPropertySubscriber<T> implements PropertySubscriber<T> {

    private final String propertyKey;

    private final Class<T> subscribeType;

    private final PropertySelector selector;

    public AbstractPropertySubscriber(String propertyKey, Class<T> subscribeType) {
        this.propertyKey = propertyKey;
        this.subscribeType = subscribeType;

        this.selector = new SinglePropertySelector(propertyKey);
    }

    @Override
    public PropertySelector selector() {
        return selector;
    }

    @Override
    public Class<?> getSubscribeType() {
        return subscribeType;
    }

}
