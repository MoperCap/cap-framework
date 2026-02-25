package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.AbstractPropertyConverter;

abstract class DefaultAbstractPropertyConverter<S, T> extends AbstractPropertyConverter<S, T> {

    public DefaultAbstractPropertyConverter(Class<S> sourceType, Class<T> targetType) {
        super(sourceType, targetType);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
