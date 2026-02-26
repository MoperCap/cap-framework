package org.moper.cap.property.resolver.converters;

public class ShortToStringConverter extends DefaultAbstractPropertyConverter<Short, String> {
    public ShortToStringConverter() {
        super(Short.class, String.class);
    }
    @Override public String convert(Short value) { return value == null ? null : value.toString(); }
}