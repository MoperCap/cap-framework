package org.moper.cap.common.converter.impl;

public class StringToFloatConverter extends DefaultAbstractTypeConverter<String, Float> {
    public StringToFloatConverter() {
        super(String.class, Float.class);
    }
    @Override public Float convert(String value) { return value == null ? null : Float.parseFloat(value.trim()); }
}