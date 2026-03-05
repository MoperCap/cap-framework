package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class DoubleToStringConverter extends DefaultAbstractTypeConverter<Double, String> {
    public DoubleToStringConverter() {
        super(Double.class, String.class);
    }
    @Override public String convert(Double value) { return value == null ? null : value.toString(); }
}