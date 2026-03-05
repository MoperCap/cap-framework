package org.moper.cap.web.binder;

import java.lang.reflect.Parameter;

public record ParameterMetadata(
        Parameter parameter,
        String name,
        Class<?> type
) {
    public ParameterMetadata {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
    }
}
