package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class StringToDoubleConverter extends DefaultAbstractTypeConverter<String, Double> {
    public StringToDoubleConverter() {
        super(String.class, Double.class);
    }
    @Override public Double convert(String value) { return value == null ? null : Double.parseDouble(value.trim()); }
}