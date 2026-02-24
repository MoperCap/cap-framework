package org.moper.cap.property.subscriber.selector;

import org.moper.cap.property.subscriber.PropertySelector;

public final class AnyPropertySelector implements PropertySelector {

    @Override
    public boolean matches(String key) {
        return true;
    }
}
