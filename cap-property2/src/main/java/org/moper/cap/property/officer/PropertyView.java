package org.moper.cap.property.officer;

import java.util.Optional;
import java.util.Set;

public interface PropertyView {

    Object getRawPropertyValue(String key);

    <T>  T getPropertyValue(String key,  Class<T> type);

    <T>  T getPropertyValueOrDefault(String key,  Class<T> type,  T defaultValue);

    <T> Optional<T> getPropertyValueOptional(String key, Class<T> type);

    boolean containsProperty( String key);

    Set<String> getAllPropertyKey();
}
