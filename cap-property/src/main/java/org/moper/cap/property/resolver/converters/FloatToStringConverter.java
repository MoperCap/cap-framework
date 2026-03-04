package org.moper.cap.property.resolver.converters;

public class FloatToStringConverter extends DefaultAbstractTypeConverter<Float, String> {
    public FloatToStringConverter() {
        super(Float.class, String.class);
    }
    @Override public String convert(Float value) { return value == null ? null : value.toString(); }
}