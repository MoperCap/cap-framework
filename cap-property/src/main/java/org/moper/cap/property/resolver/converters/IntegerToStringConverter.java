package org.moper.cap.property.resolver.converters;

public class IntegerToStringConverter extends DefaultAbstractPropertyConverter<Integer, String> {
    public IntegerToStringConverter() {
        super(Integer.class, String.class);
    }
    @Override public String convert(Integer value) { return value == null ? null : value.toString(); }
}