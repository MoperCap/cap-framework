package org.moper.cap.bean.util;

import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.bean.exception.BeanDefinitionException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Bean 名称解析器，用于从类、方法或构造函数/方法参数的注解中提取 Bean 名称。
 */
public final class BeanNamesResolver {

    /**
     * 解析 @Capper 标注的类，获取 Bean 名称列表。
     *
     * <p>若 @Capper 注解上的 names 属性设置了一个名称，则该名称作为 Bean 名称，无别名。
     * 若 @Capper 注解上的 names 属性设置了多个名称，则第一个名称作为主要 Bean 名称，后续名称作为别名。
     * 若 @Capper 注解未设置或者 names 属性未设置或为空，则使用默认名称：类名首字母小写。
     *
     * @param clazz 类的反射对象，不能为 null
     * @return Bean 名称数组（第一个为主名称，其余为别名），至少包含一个名称
     */
    public static String[] resolveClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class must not be null");
        }
        String defaultName = decapitalize(clazz.getSimpleName());
        Capper capper = clazz.getAnnotation(Capper.class);
        if (capper == null) {
            return new String[]{defaultName};
        }
        String[] beanNames = capper.names();
        if (beanNames == null || beanNames.length == 0) {
            return new String[]{defaultName};
        }

        Set<String> resultSet = new LinkedHashSet<>();
        for (String beanName : beanNames) {
            if(beanName != null && !beanName.isBlank())
                resultSet.add(beanName);
        }
        return resultSet.toArray(new String[0]);
    }

    /**
     * 解析 @Capper 标注的方法，获取 Bean 名称列表。
     *
     * <p>若 @Capper 注解上的 names 属性设置了一个名称，则该名称作为 Bean 名称，无别名。
     * 若 @Capper 注解上的 names 属性设置了多个名称，则第一个名称作为主要 Bean 名称，后续名称作为别名。
     * 若 @Capper 注解未设置或者 names 属性未设置或为空，则使用默认名称：方法名首字母小写。
     *
     * @param method 方法的反射对象，不能为 null
     * @return Bean 名称数组（第一个为主名称，其余为别名），至少包含一个名称
     */
    public static String[] resolveMethod(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Method must not be null");
        }
        String defaultName = decapitalize(method.getName());
        Capper capper = method.getAnnotation(Capper.class);
        if (capper == null) {
            return new String[]{defaultName};
        }
        String[] beanNames = capper.names();
        if(beanNames == null || beanNames.length == 0) {
            return new String[]{defaultName};
        }

        Set<String> resultSet = new LinkedHashSet<>();
        for (String beanName : beanNames) {
            if (beanName != null && !beanName.isBlank())
                resultSet.add(beanName);
        }
        return resultSet.toArray(new String[0]);
    }

    /**
     * 解析单个参数对应的 Bean 名称。
     *
     * <p>参数推导优先级（按顺序）：
     * <ol>
     *   <li>参数的 @Inject 注解中显式指定的 beanName（非空时）</li>
     *   <li>参数类型的简单类名首字母小写（如 {@code MyService → myService}）</li>
     * </ol>
     *
     * @param parameter 参数反射对象，不能为 null
     * @return 单个 Bean 名称字符串
     * @throws BeanDefinitionException 如果参数无法推导 Bean 名称
     */
    public static String resolveParameter(Parameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }
        // 优先级 1：@Inject 注解中显式指定的 beanName
        Inject inject = parameter.getAnnotation(Inject.class);
        if (inject != null && !inject.beanName().isBlank()) {
            return inject.beanName();
        }
        // 优先级 2：参数类型的简单类名首字母小写
        String simpleName = parameter.getType().getSimpleName();
        if (!simpleName.isBlank()) {
            return decapitalize(simpleName);
        }
        // 无法推导
        Parameter[] params = parameter.getDeclaringExecutable().getParameters();
        int index = 0;
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals(parameter)) {
                index = i;
                break;
            }
        }
        String ownerName = parameter.getDeclaringExecutable().getName();
        throw new BeanDefinitionException(
                "Cannot resolve bean name for parameter " + index + " of '" + ownerName +
                "' (type: " + parameter.getType().getName() + "): parameter type has no simple name. " +
                "Use @Inject(beanName=\"...\") to specify explicitly.");
    }

    /**
     * 将类名或方法名首字母小写作为默认 Bean 名称。
     *
     * @param name 类名或方法名
     * @return 首字母小写的名称
     */
    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        if (Character.isLowerCase(name.charAt(0))) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * 解析 @Inject 标注的字段，获取 Bean 名称。
     *
     * <p>字段推导优先级（按顺序）：
     * <ol>
     *   <li>字段的 @Inject 注解中显式指定的 beanName（非空时）</li>
     *   <li>字段类型的简单类名首字母小写（如 {@code MyService → myService}）</li>
     * </ol>
     *
     * @param field 字段反射对象，不能为 null
     * @return 单个 Bean 名称字符串
     * @throws BeanDefinitionException 如果字段无法推导 Bean 名称
     */
    public static String resolveField(Field field) {
        if (field == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
        Inject inject = field.getAnnotation(Inject.class);
        if (inject != null && !inject.beanName().isBlank()) {
            return inject.beanName();
        }
        String simpleName = field.getType().getSimpleName();
        if (!simpleName.isBlank()) {
            return decapitalize(simpleName);
        }
        throw new BeanDefinitionException(
                "Cannot resolve bean name for field '" + field.getName() +
                "' (type: " + field.getType().getName() + "): field type has no simple name. " +
                "Use @Inject(beanName=\"...\") to specify explicitly.");
    }
}
