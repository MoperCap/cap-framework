package org.moper.cap.property.event;

import java.util.Objects;

public record PropertyRemoveOperation(
         String key
) implements PropertyOperation {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PropertyRemoveOperation that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
