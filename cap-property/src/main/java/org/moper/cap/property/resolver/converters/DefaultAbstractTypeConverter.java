package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.AbstractTypeConverter;

abstract class DefaultAbstractTypeConverter<S, T> extends AbstractTypeConverter<S, T> {

    public DefaultAbstractTypeConverter(Class<S> sourceType, Class<T> targetType) {
        super(sourceType, targetType);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
