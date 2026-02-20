package org.moper.cap.property.event;

import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record PropertySetOperation(
        @NotBlank String key,
        @Nullable Object value
) implements PropertyOperation {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PropertySetOperation that)) return false;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
