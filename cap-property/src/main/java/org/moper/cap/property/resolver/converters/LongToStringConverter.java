package org.moper.cap.property.resolver.converters;

public class LongToStringConverter extends DefaultAbstractTypeConverter<Long, String> {
    public LongToStringConverter() {
        super(Long.class, String.class);
    }
    @Override public String convert(Long value) { return value == null ? null : value.toString(); }
}