package org.moper.cap.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.property.publisher.PropertyPublisher;

import java.time.Instant;
import java.util.Objects;

/**
 * 属性字段
 *
 * @param name 属性名
 * @param value 属性值
 * @param publisher 属性发布者
 * @param lastModified 最后修改时间
 */
public record PropertyDefinition(
        @NotBlank String name,
        @Nullable Object value,
        @NotNull PropertyPublisher publisher,
        @NotNull Instant lastModified
) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PropertyDefinition that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(publisher, that.publisher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, publisher);
    }
}
