package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

public class ByteToStringConverter implements PropertyConverter<Byte, String> {
    @Override public Class<Byte> getSourceType() { return Byte.class; }
    @Override public Class<String> getTargetType() { return String.class; }
    @Override public String convert(Byte value) { return value == null ? null : value.toString(); }
}