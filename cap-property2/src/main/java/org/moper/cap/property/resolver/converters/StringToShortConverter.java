package org.moper.cap.property.resolver.converters;

public class StringToShortConverter extends DefaultAbstractPropertyConverter<String, Short> {
    public StringToShortConverter() {
        super(String.class, Short.class);
    }
    @Override public Short convert(String value) { return value == null ? null : Short.parseShort(value.trim()); }
}