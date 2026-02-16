package org.moper.cap.property.event;

public record PropertySetOperation(
        String source,
        String key,
        Object value
) implements PropertyOperation {
}
