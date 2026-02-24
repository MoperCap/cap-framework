package org.moper.cap.property.event;

public record PropertySetOperation(
        String key,
        Object value
) implements PropertyOperation {

    public PropertySetOperation {
        if(key == null || key.isBlank()){
            throw new IllegalArgumentException("PropertySetOperation Key cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return "PropertySetOperation{" +
                "key='" + key + '\'' +
                ", value=" + value.toString() +
                '}';
    }
}
