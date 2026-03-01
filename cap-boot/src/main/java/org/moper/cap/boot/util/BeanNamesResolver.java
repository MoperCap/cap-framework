package org.moper.cap.boot.util;

import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.bean.exception.BeanDefinitionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
        if (beanNames == null || beanNames.length == 0 || (beanNames.length == 1 && beanNames[0].isEmpty())) {
            return new String[]{defaultName};
        }
        return beanNames;
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
        if (beanNames == null || beanNames.length == 0 || (beanNames.length == 1 && beanNames[0].isEmpty())) {
            return new String[]{defaultName};
        }
        return beanNames;
    }

    /**
     * 解析构造函数的参数，获取每个参数对应的 Bean 名称数组。
     *
     * <p>参数推导优先级（按顺序）：
     * <ol>
     *   <li>参数的 @Inject 注解中显式指定的 beanName（非空时）</li>
     *   <li>参数类型的简单类名首字母小写（如 {@code MyService → myService}）</li>
     * </ol>
     *
     * @param constructor 构造函数的反射对象，不能为 null
     * @return 参数 Bean 名称数组，长度等于参数个数，按参数顺序排列
     * @throws BeanDefinitionException 如果某个参数无法推导 Bean 名称
     */
    public static String[] resolveParameter(Constructor<?> constructor) {
        if (constructor == null) {
            throw new IllegalArgumentException("Constructor must not be null");
        }
        Parameter[] parameters = constructor.getParameters();
        String[] beanNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            beanNames[i] = resolveParameterBeanName(parameters[i], constructor.getDeclaringClass().getSimpleName(), i);
        }
        return beanNames;
    }

    /**
     * 解析工厂方法的参数，获取每个参数对应的 Bean 名称数组。
     *
     * <p>参数推导优先级（按顺序）：
     * <ol>
     *   <li>参数的 @Inject 注解中显式指定的 beanName（非空时）</li>
     *   <li>参数类型的简单类名首字母小写（如 {@code MyService → myService}）</li>
     * </ol>
     *
     * @param method 方法的反射对象，不能为 null
     * @return 参数 Bean 名称数组，长度等于参数个数，按参数顺序排列
     * @throws BeanDefinitionException 如果某个参数无法推导 Bean 名称
     */
    public static String[] resolveParameter(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Method must not be null");
        }
        Parameter[] parameters = method.getParameters();
        String[] beanNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            beanNames[i] = resolveParameterBeanName(parameters[i], method.getName(), i);
        }
        return beanNames;
    }

    /**
     * 解析单个参数对应的 Bean 名称。
     *
     * @param parameter 参数反射对象
     * @param ownerName 参数所属的方法或构造函数名称（用于异常信息）
     * @param index     参数的索引（用于异常信息）
     * @return Bean 名称
     * @throws BeanDefinitionException 如果参数无法推导 Bean 名称
     */
    private static String resolveParameterBeanName(Parameter parameter, String ownerName, int index) {
        // 优先级 1：@Inject 注解中显式指定的 beanName
        Inject inject = parameter.getAnnotation(Inject.class);
        if (inject != null && !inject.beanName().isEmpty()) {
            return inject.beanName();
        }
        // 优先级 2：参数类型的简单类名首字母小写
        String simpleName = parameter.getType().getSimpleName();
        if (simpleName != null && !simpleName.isEmpty()) {
            return decapitalize(simpleName);
        }
        // 无法推导
        throw new BeanDefinitionException(
                "Cannot resolve bean name for parameter " + index + " of '" + ownerName +
                "': parameter type has no simple name. Use @Inject(beanName=\"...\") to specify explicitly.");
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
}
