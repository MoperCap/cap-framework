package org.moper.cap.property.resolver.converters;

public class BooleanToStringConverter extends DefaultAbstractPropertyConverter<Boolean, String> {

    public BooleanToStringConverter() {
        super(Boolean.class, String.class);
    }

    @Override public String convert(Boolean value) { return value == null ? null : value.toString(); }
}