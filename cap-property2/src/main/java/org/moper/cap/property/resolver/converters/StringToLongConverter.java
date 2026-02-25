package org.moper.cap.property.resolver.converters;

public class StringToLongConverter extends DefaultAbstractPropertyConverter<String, Long> {
    public StringToLongConverter() {
        super(String.class, Long.class);
    }
    @Override public Long convert(String value) { return value == null ? null : Long.parseLong(value.trim()); }
}