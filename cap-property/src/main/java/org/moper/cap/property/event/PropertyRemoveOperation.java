package org.moper.cap.property.event;

public record PropertyRemoveOperation(
        String source,
        String key
) implements PropertyOperation {
}
