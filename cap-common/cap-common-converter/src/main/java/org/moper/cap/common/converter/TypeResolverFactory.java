package org.moper.cap.common.converter;

import org.moper.cap.common.converter.impl.DefaultTypeResolver;

/**
 * TypeResolver 单例工厂
 * 
 * 类似 LoggerFactory，提供全局单例 TypeResolver
 * 后续可演进为真正的工厂模式
 */
public class TypeResolverFactory {

    private static volatile TypeResolver instance;

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
