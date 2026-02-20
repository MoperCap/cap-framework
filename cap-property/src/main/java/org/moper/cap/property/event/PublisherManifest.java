package org.moper.cap.property.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.property.publisher.PropertyPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record PublisherManifest(
        @NotNull PropertyPublisher publisher,
        int version,
        @NotEmpty List<PropertyOperation> operations,
        @NotNull Instant timestamp
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
