package org.moper.cap.property.subscriber.impl;

import org.moper.cap.property.subscriber.PropertySelector;
import org.moper.cap.property.subscriber.PropertySubscriber;

public abstract class DefaultAbstractPropertySubscriber<T> implements PropertySubscriber<T> {

    private final Class<T> subscribeType;

    private final PropertySelector selector;

    public DefaultAbstractPropertySubscriber(String propertyKey, Class<T> subscribeType) {
        if(propertyKey == null || propertyKey.isBlank()) {
            throw new IllegalArgumentException("PropertySubscriber propertyKey cannot be null or blank");
        }

        this.subscribeType = subscribeType;
        this.selector = new SinglePropertySelector(propertyKey);
    }

    @Override
    public PropertySelector selector() {
        return selector;
    }

    @Override
    public Class<T> getSubscribeType() {
        return subscribeType;
    }

}
