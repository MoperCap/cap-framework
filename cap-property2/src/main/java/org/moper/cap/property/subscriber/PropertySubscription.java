package org.moper.cap.property.subscriber;

import org.moper.cap.core.context.ResourceContext;

public interface PropertySubscription extends ResourceContext, Iterable<PropertySubscriber<?>> {
    String name();
}
