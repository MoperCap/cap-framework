package org.moper.cap.property.subscriber.selector;

import org.moper.cap.property.subscriber.PropertySelector;

public class SinglePropertySelector implements PropertySelector {

    private final String propertyKey;

    public SinglePropertySelector(final String propertyKey) {
        if(propertyKey == null || propertyKey.isBlank()) {
            throw new IllegalArgumentException("SinglePropertySelector propertyKey cannot be null or blank");
        }

        this.propertyKey = propertyKey;
    }

    @Override
    public boolean matches(String key) {
        return propertyKey.equals(key);
    }
}
