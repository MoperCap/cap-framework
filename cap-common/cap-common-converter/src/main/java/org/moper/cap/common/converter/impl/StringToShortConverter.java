package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class StringToShortConverter extends DefaultAbstractTypeConverter<String, Short> {
    public StringToShortConverter() {
        super(String.class, Short.class);
    }
    @Override public Short convert(String value) { return value == null ? null : Short.parseShort(value.trim()); }
}