package org.moper.cap.property.resolver.converters;

public class StringToFloatConverter extends DefaultAbstractPropertyConverter<String, Float> {
    public StringToFloatConverter() {
        super(String.class, Float.class);
    }
    @Override public Float convert(String value) { return value == null ? null : Float.parseFloat(value.trim()); }
}