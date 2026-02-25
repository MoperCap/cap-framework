package org.moper.cap.property.subscriber;

import org.moper.cap.core.context.ResourceContext;
import org.moper.cap.property.event.PropertyOperation;

public interface PropertySubscription extends ResourceContext {

    PropertySelector getSelector();

    void dispatch(PropertyOperation... operations);
}
