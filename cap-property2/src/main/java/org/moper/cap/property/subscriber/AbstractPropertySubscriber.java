package org.moper.cap.property.subscriber;

public abstract class AbstractPropertySubscriber<T> implements PropertySubscriber<T> {
    private final String propertyKey;

    public AbstractPropertySubscriber(String propertyKey) {
        if(propertyKey == null || propertyKey.isBlank()){
            throw new IllegalArgumentException("PropertySubscriber Key cannot be null or blank");
        }
        this.propertyKey = propertyKey;
    }

    @Override
    public String getPropertyKey() {
        return propertyKey;
    }
}
