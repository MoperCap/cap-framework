package org.moper.cap.property.resolver.converters;

import org.moper.cap.property.resolver.PropertyConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 属性系统基础类型转换器合集工具类。
 * 提供所有Java常用基本类型转换器的注册与迭代获取。
 */
public final class BasicConverters {
    private static final List<PropertyConverter<?, ?>> BASIC_CONVERTERS =
            Collections.unmodifiableList(Arrays.asList(
                    new StringToIntegerConverter(),
                    new StringToLongConverter(),
                    new StringToBooleanConverter(),
                    new StringToDoubleConverter(),
                    new StringToFloatConverter(),
                    new StringToShortConverter(),
                    new StringToByteConverter(),
                    new IntegerToStringConverter(),
                    new LongToStringConverter(),
                    new DoubleToStringConverter(),
                    new BooleanToStringConverter(),
                    new FloatToStringConverter(),
                    new ShortToStringConverter(),
                    new ByteToStringConverter()
            ));

    private BasicConverters() {}

    public static List<PropertyConverter<?, ?>> getAllBasicConverters() {
        return BASIC_CONVERTERS;
    }

}
