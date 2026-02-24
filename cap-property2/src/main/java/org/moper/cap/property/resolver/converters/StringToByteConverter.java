package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class StringToByteConverter implements PropertyConverter<String, Byte> {
    @Override public Class<String> getSourceType() { return String.class; }
    @Override public Class<Byte> getTargetType() { return Byte.class; }
    @Override public Byte convert(String value) { return value == null ? null : Byte.parseByte(value.trim()); }
}