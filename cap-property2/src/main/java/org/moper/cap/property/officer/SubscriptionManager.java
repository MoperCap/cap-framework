package org.moper.cap.property.officer;

import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.Collection;
import java.util.function.Supplier;

public interface SubscriptionManager {

    PropertySubscription createSubscription(Supplier<PropertySubscription> supplier);

    boolean containsSubscription(PropertySubscription subscription);

    void destroySubscription(PropertySubscription subscription);

    Collection<PropertySubscription> getAllSubscriptions();
}
