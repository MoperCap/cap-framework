package org.moper.cap.property.event;

public record PropertyRemoveOperation(
        String key
) implements PropertyOperation {

    public PropertyRemoveOperation {
        if(key == null || key.isBlank()) {
            throw new IllegalArgumentException("PropertyRemoveOperation Key cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return "PropertyRemoveOperation{" +
                "key='" + key + '\'' +
                '}';
    }
}
