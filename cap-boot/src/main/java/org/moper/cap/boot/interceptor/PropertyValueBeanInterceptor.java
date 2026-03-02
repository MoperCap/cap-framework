package org.moper.cap.boot.interceptor;

import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.property.annotation.Value;
import org.moper.cap.property.officer.PropertyOfficer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Value @Value} 字段注入拦截器。
 *
 * <p>在实例化之后阶段扫描 Bean 类中所有标注了 {@link Value} 的字段，
 * 解析 {@code ${key}} 或 {@code ${key:defaultValue}} 表达式，
 * 并从 {@link PropertyOfficer} 中获取属性值注入字段。
 */
public class PropertyValueBeanInterceptor implements BeanInterceptor {

    private static final Pattern VALUE_PATTERN = Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?\\}");

    private final PropertyOfficer propertyOfficer;

    public PropertyValueBeanInterceptor(PropertyOfficer propertyOfficer) {
        this.propertyOfficer = propertyOfficer;
    }

    @Override
    public Object afterInstantiation(Object bean, BeanDefinition definition) throws BeanException {
        for (Field field : getAllFields(bean.getClass())) {
            Value annotation = field.getAnnotation(Value.class);
            if (annotation == null) continue;
            injectValueField(bean, field, annotation.value());
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    private void injectValueField(Object bean, Field field, String expression) {
        Matcher matcher = VALUE_PATTERN.matcher(expression);
        if (!matcher.matches()) {
            throw new BeanException(
                    "Invalid @Value expression '" + expression + "' on field '"
                    + field.getName() + "' in " + bean.getClass().getName());
        }

        String key = matcher.group(1);
        String defaultStr = matcher.group(2);

        try {
            field.setAccessible(true);
            Object value;
            if (defaultStr != null) {
                value = propertyOfficer.getPropertyValueOrDefault(key, (Class) field.getType(), defaultStr);
            } else {
                value = propertyOfficer.getPropertyValue(key, (Class) field.getType());
            }
            field.set(bean, value);
        } catch (BeanException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanException(
                    "Failed to inject @Value field '" + field.getName()
                    + "' in " + bean.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 200;
    }
}
