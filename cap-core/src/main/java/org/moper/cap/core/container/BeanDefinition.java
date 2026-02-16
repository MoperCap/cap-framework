package org.moper.cap.core.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Bean字段
 * @param name BeanName(全局唯一)
 * @param type Bean声明类型
 * @param constructor Bean构造函数
 * @param factory Bean工厂函数
 * @param init Bean初始化方法
 * @param destroy Bean销毁方法
 * @param singleton Bean实例是否为单例
 * @param lazy Bean实例是否懒加载
 */
public record BeanDefinition(
        @NotBlank String name,
        @NotNull Class<?> type,
        @Nullable Constructor<?> constructor,
        @Nullable Method factory,
        @Nullable Method init,
        @Nullable Method destroy,
        boolean singleton,
        boolean lazy
){}
