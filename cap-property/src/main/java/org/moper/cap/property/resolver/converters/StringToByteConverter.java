package org.moper.cap.property.resolver.converters;

public class StringToByteConverter extends DefaultAbstractPropertyConverter<String, Byte> {
    public StringToByteConverter() {
        super(String.class, Byte.class);
    }
    @Override public Byte convert(String value) { return value == null ? null : Byte.parseByte(value.trim()); }
}