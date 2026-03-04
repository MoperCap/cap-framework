package org.moper.cap.common.converter.impl;

import org.moper.cap.common.annotation.Priority;

@Priority(100)
public class ByteToStringConverter extends DefaultAbstractTypeConverter<Byte, String> {
    public ByteToStringConverter() {
        super(Byte.class, String.class);
    }
    @Override public String convert(Byte value) { return value == null ? null : value.toString(); }
}