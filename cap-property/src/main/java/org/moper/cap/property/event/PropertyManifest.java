package org.moper.cap.property.event;

import java.time.Instant;
import java.util.List;

public record PropertyManifest(
        String source,
        long fromVersion,
        long toVersion,
        List<PropertyOperation> operations,
        Instant timestamp
) implements PropertyEvent {}
