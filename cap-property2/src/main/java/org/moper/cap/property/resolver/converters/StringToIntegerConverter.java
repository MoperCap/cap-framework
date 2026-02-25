package org.moper.cap.property.resolver.converters;

public class StringToIntegerConverter extends DefaultAbstractPropertyConverter<String, Integer> {
    public StringToIntegerConverter() {
        super(String.class, Integer.class);
    }
    @Override public Integer convert(String value) { return value == null ? null : Integer.parseInt(value.trim()); }
}