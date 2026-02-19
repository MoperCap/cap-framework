package org.moper.cap.property.subscriber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.core.context.ResourceViewContext;
import org.moper.cap.property.PropertyDefinition;
import org.moper.cap.property.subscriber.selector.AnyPropertySelector;

import java.util.Optional;
import java.util.Set;

/**
 * 属性只读视图池
 */
public interface PropertyViewPool extends PropertySubscription, ResourceViewContext {

    @Nullable Object getRawPropertyValue(@NotBlank String key);

    <T> @Nullable T getPropertyValue(@NotBlank String key, @NotNull Class<T> type);

    <T> @NotNull T getPropertyValueOrDefault(@NotBlank String key, @NotNull Class<T> type, @NotNull T defaultValue);

    <T> @NotNull Optional<T> getPropertyValueOptional(@NotBlank String key, @NotNull Class<T> type);

    boolean containsProperty(@NotBlank String key);

    @NotNull
    Set<String> keySet();

    @Override
    default PropertySelector selector(){
        return new AnyPropertySelector();
    }
}
