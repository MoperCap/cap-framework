package org.moper.cap.common.converter;

import org.moper.cap.common.converter.impl.DefaultTypeResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * TypeResolver 单例工厂
 * 
 * 类似 LoggerFactory，提供全局单例 TypeResolver
 * 后续可演进为真正的工厂模式
 */
public class TypeResolverFactory {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<>();
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<>();

    static {
        PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
        PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
        PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
        PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
        PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
        PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
        PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);

        WRAPPER_TO_PRIMITIVE.put(Byte.class, byte.class);
        WRAPPER_TO_PRIMITIVE.put(Short.class, short.class);
        WRAPPER_TO_PRIMITIVE.put(Integer.class, int.class);
        WRAPPER_TO_PRIMITIVE.put(Long.class, long.class);
        WRAPPER_TO_PRIMITIVE.put(Float.class, float.class);
        WRAPPER_TO_PRIMITIVE.put(Double.class, double.class);
        WRAPPER_TO_PRIMITIVE.put(Boolean.class, boolean.class);
        WRAPPER_TO_PRIMITIVE.put(Character.class, char.class);
    }

    private static volatile TypeResolver instance;

    /**
     * 获取基本类型对应的包装类型，若已是包装类型则原样返回。
     */
    public static Class<?> getWrapperType(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return PRIMITIVE_TO_WRAPPER.get(type);
    }

    /**
     * 获取包装类型对应的基本类型，若无对应关系（即非包装类型）则返回 null。
     */
    public static Class<?> getPrimitiveType(Class<?> type) {
        return WRAPPER_TO_PRIMITIVE.get(type);
    }

    /**
     * 判断两个类型是否等价（考虑基本类型和对应包装类型）。
     */
    public static boolean isEquivalent(Class<?> type1, Class<?> type2) {
        if (type1 == type2) {
            return true;
        }
        Class<?> wrapper1 = getWrapperType(type1);
        Class<?> wrapper2 = getWrapperType(type2);
        return wrapper1 == wrapper2;
    }

    /**
     * 获取单例 TypeResolver
     */
    public static TypeResolver getTypeResolver() {
        if (instance == null) {
            synchronized (TypeResolverFactory.class) {
                if (instance == null) {
                    instance = new DefaultTypeResolver();
                }
            }
        }
        return instance;
    }

    /**
     * 设置自定义 TypeResolver（仅在初始化阶段使用）
     */
    public static void setTypeResolver(TypeResolver typeResolver) {
        if (typeResolver == null) {
            throw new IllegalArgumentException("typeResolver cannot be null");
        }
        synchronized (TypeResolverFactory.class) {
            instance = typeResolver;
        }
    }

    private TypeResolverFactory() {
    }
}
