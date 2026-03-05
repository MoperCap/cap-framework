package org.moper.cap.common.converter.impl;

import org.moper.cap.common.priority.Priority;

@Priority(100)
public class StringToByteConverter extends DefaultAbstractTypeConverter<String, Byte> {
    public StringToByteConverter() {
        super(String.class, Byte.class);
    }
    @Override public Byte convert(String value) { return value == null ? null : Byte.parseByte(value.trim()); }
}