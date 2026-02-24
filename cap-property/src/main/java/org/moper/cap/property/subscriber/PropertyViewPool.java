package org.moper.cap.property.subscriber;

import org.moper.cap.property.subscriber.selector.AnyPropertySelector;

import java.util.Optional;
import java.util.Set;

/**
 * 属性只读视图池
 */
public interface PropertyViewPool extends PropertySubscription {

     Object getRawPropertyValue( String key);

    <T>  T getPropertyValue( String key,  Class<T> type);

    <T>  T getPropertyValueOrDefault( String key,  Class<T> type,  T defaultValue);

    <T>  Optional<T> getPropertyValueOptional( String key,  Class<T> type);

    boolean containsProperty( String key);
    
    Set<String> keySet();

    @Override
    default PropertySelector selector(){
        return new AnyPropertySelector();
    }
}
