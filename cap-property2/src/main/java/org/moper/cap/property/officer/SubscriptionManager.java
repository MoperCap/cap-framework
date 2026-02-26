package org.moper.cap.property.officer;

import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.Collection;
import java.util.function.Supplier;

public interface SubscriptionManager {

    PropertySubscription createSubscription(String name);

    PropertySubscription createSubscription(String name, Supplier<PropertySubscription> supplier);

    boolean containsSubscription(String name);

    void destroySubscription(String name);

    Collection<PropertySubscription> getAllSubscriptions();
}
