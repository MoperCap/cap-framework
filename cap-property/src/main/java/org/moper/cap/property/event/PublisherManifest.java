package org.moper.cap.property.event;

import org.moper.cap.property.publisher.PropertyPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record PublisherManifest(
        PropertyPublisher publisher,
        int version,
        List<PropertyOperation> operations,
        Instant timestamp
) implements PropertyEvent {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PublisherManifest that)) return false;
        return version == that.version && Objects.equals(publisher, that.publisher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publisher, version);
    }
}
