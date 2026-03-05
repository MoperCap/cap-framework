package org.moper.cap.common.converter.impl;

import org.moper.cap.common.converter.AbstractTypeConverter;

abstract class DefaultAbstractTypeConverter<S, T> extends AbstractTypeConverter<S, T> {

    public DefaultAbstractTypeConverter(Class<S> sourceType, Class<T> targetType) {
        super(sourceType, targetType);
    }
}
