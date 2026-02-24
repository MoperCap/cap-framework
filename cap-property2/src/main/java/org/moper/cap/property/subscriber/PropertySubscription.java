package org.moper.cap.property.subscriber;

import org.moper.cap.property.event.PropertyOperation;

public interface PropertySubscription {

    PropertySelector getSelector();

    void dispatch(PropertyOperation... operations);
}
