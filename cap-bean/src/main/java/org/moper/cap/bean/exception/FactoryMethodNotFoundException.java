package org.moper.cap.bean.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 工厂方法未找到异常
 * 在尝试获取指定的工厂方法时找不到对应的方法
 */
public class FactoryMethodNotFoundException extends BeanCreationException {
    
    public FactoryMethodNotFoundException(@NotBlank String beanName,
                                         @NotBlank String factoryMethodName,
                                         @NotNull Class<?> factoryClass,
                                         @NotNull Class<?>[] parameterTypes) {
        super(beanName, "No factory method '" + factoryMethodName + 
              "' found in class '" + factoryClass.getName() + 
              "' with parameter types [" + formatParameterTypes(parameterTypes) + "]");
    }

    public FactoryMethodNotFoundException(@NotBlank String beanName,
                                         @NotBlank String factoryMethodName,
                                         @NotBlank String factoryBeanName,
                                         @NotNull Class<?>[] parameterTypes) {
        super(beanName, "No factory method '" + factoryMethodName + 
              "' found in factory bean '" + factoryBeanName + 
              "' with parameter types [" + formatParameterTypes(parameterTypes) + "]");
    }

    private static String formatParameterTypes(Class<?>[] parameterTypes) {
        if (parameterTypes.length == 0) {
            return "";
        }
        return Arrays.stream(parameterTypes)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }
}