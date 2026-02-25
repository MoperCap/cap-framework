package org.moper.cap.property.resolver.converters;

public class StringToBooleanConverter extends DefaultAbstractPropertyConverter<String, Boolean> {
    public StringToBooleanConverter() {
        super(String.class, Boolean.class);
    }
    @Override public Boolean convert(String value) { return value == null ? null : Boolean.parseBoolean(value.trim()); }
}