package org.moper.cap.property.subscriber.impl;

import org.moper.cap.property.subscriber.PropertySelector;

public final class AnyPropertySelector implements PropertySelector {

    @Override
    public boolean matches(String key) {
        return true;
    }
}
