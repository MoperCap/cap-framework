package org.moper.cap.property.subscriber.selector;

import org.moper.cap.property.subscriber.PropertySelector;

import java.util.Set;

public final class ExactPropertySelector implements PropertySelector {

    private final Set<String> keys;

    public ExactPropertySelector(Set<String> keys) {
        this.keys = keys;
    }

    @Override
    public boolean matches(String key) {
        return keys.contains(key);
    }

}
