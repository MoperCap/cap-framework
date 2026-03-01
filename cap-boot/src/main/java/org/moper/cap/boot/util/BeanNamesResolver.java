package org.moper.cap.boot.util;

import org.moper.cap.bean.annotation.Capper;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Bean名称解析器，用于从类或方法的注解中提取Bean名称 </br>
 */
public final class BeanNamesResolver {


    /**
     * 解析类或方法上的 @Capper 注解，获取 Bean 名称列表 </br>
     *
     * 若 @Capper 注解上的 names 属性设置了一个名称，则该名称作为 Bean 名称，无别名。</p>
     * 若 @Capper 注解上的 names 属性设置了多个名称，则第一个名称作为主要 Bean 名称，后续名称作为别名。</p>
     * 若 @Capper 注解未设置或者 @Capper 注解上的 names 属性未设置或为空，则使用默认名称：类名或方法名首字母小写。</p>
     *
     * @param element 类或方法的反射对象
     * @return Bean 名称列表，至少包含一个名称
     */
    public static String[] resolve(AnnotatedElement element){
        if(element == null){
            throw new IllegalArgumentException("AnnotatedElement must not be null");
        }

        String defaultName = null;
        switch (element){
            case Class<?> clazz -> defaultName = decapitalize(clazz.getSimpleName());
            case Method method -> defaultName = decapitalize(method.getName());
            default -> throw new IllegalStateException("Unexpected value: " + element);
        }

        Capper capper = element.getAnnotation(Capper.class);
        if(capper == null) {
            return new String[]{defaultName};
        }
        String[] beanNames = capper.names();
        if(beanNames == null || beanNames.length == 0) {
            return new String[]{defaultName};
        }
        return beanNames;
    }

    /**
     * 将类名或方法名首字母小写作为默认 Bean 名称
     *
     * @param name 类名或方法名
     * @return 首字母小写的名称
     */
    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        if (Character.isLowerCase(name.charAt(0))) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
