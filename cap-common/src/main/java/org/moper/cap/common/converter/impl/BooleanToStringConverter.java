package org.moper.cap.common.converter.impl;

public class BooleanToStringConverter extends DefaultAbstractTypeConverter<Boolean, String> {

    public BooleanToStringConverter() {
        super(Boolean.class, String.class);
    }

    @Override public String convert(Boolean value) { return value == null ? null : value.toString(); }
}