package org.moper.cap.property.officer;

import org.moper.cap.property.publisher.PropertyPublisher;

import java.util.Collection;
import java.util.function.Supplier;

interface PublisherManager {
    PropertyPublisher getPublisher(String name);

    PropertyPublisher getPublisher(String name, Supplier<PropertyPublisher> supplier);

    boolean containsPublisher(String name);

    void destroyPublisher(String name);

    Collection<PropertyPublisher> getAllPublishers();
}
