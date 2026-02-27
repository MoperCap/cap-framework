package org.moper.cap.property.subscriber.impl;

import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DefaultPropertySubscription implements PropertySubscription {
    private final String name;
    private final List<PropertySubscriber<?>> subscribers;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DefaultPropertySubscription(String name, List<PropertySubscriber<?>> subscribers) {
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("DefaultPropertySubscription name cannot be null or blank");
        }

        if(subscribers == null || subscribers.isEmpty()) {
            throw new IllegalArgumentException("DefaultPropertySubscription subscribers list cannot be null or empty");
        }

        this.name = name;
        this.subscribers = subscribers;
    }


    /**
     * 获取属性订阅客户端名称
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<PropertySubscriber<?>> iterator() {
        if(isClosed())
            throw new IllegalStateException("Subscription is closed");
        return subscribers.iterator();
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() throws Exception {
        if(!closed.compareAndSet(false, true)) {
            return;
        }

        for(PropertySubscriber<?> subscriber : subscribers) {
            subscriber.onRemoved();
        }
    }
}
