package org.moper.cap.property.subscriber;

import org.moper.cap.property.subscriber.selector.ExactPropertySelector;

import java.util.Collection;
import java.util.Set;

public abstract class AbstractPropertySubscription implements PropertySubscription {
    private final PropertySelector selector;

    private final Collection<PropertySubscriber> subscribers;

    protected AbstractPropertySubscription(Collection<PropertySubscriber> subscribers) {
        if(subscribers == null || subscribers.isEmpty()) {
            throw new IllegalArgumentException("PropertySubscription subscribers must not be null or empty");
        }

        Set<String> propertyKeys = subscribers.stream()
                .map(PropertySubscriber::getPropertyKey)
                .collect(java.util.stream.Collectors.toSet());

        this.selector = new ExactPropertySelector(propertyKeys);
        this.subscribers = subscribers;
    }

    protected AbstractPropertySubscription(PropertySelector selector, Collection<PropertySubscriber> subscribers) {
        if(selector == null) {
            throw new IllegalArgumentException("PropertySubscription selector must not be null");
        }

        if(subscribers == null || subscribers.isEmpty()) {
            throw new IllegalArgumentException("PropertySubscription subscribers must not be null or empty");
        }

        this.selector = selector;
        this.subscribers = subscribers;
    }

    @Override
    public PropertySelector getSelector() {
        return selector;
    }
}
