package org.moper.cap.property.resolver.converters;

public class ByteToStringConverter extends DefaultAbstractPropertyConverter<Byte, String> {
    public ByteToStringConverter() {
        super(Byte.class, String.class);
    }
    @Override public String convert(Byte value) { return value == null ? null : value.toString(); }
}