package org.moper.cap.boot.initializer;

import org.moper.cap.annotation.Autowired;
import org.moper.cap.annotation.Qualifier;
import org.moper.cap.annotation.Value;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.environment.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * 处理 {@link Autowired} 和 {@link Value} 注解的 Bean 拦截器 </br>
 * 在 afterPropertyInjection 阶段完成字段注入
 */
public class AutowiredBeanInterceptor implements BeanInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AutowiredBeanInterceptor.class);

    private final BeanContainer beanContainer;
    private final Environment environment;

    public AutowiredBeanInterceptor(BeanContainer beanContainer, Environment environment) {
        this.beanContainer = beanContainer;
        this.environment = environment;
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        Class<?> beanClass = bean.getClass();
        for (Field field : getAllFields(beanClass)) {
            try {
                if (field.isAnnotationPresent(Autowired.class)) {
                    injectAutowired(bean, field);
                } else if (field.isAnnotationPresent(Value.class)) {
                    injectValue(bean, field);
                }
            } catch (BeanException e) {
                throw e;
            } catch (Exception e) {
                throw new BeanException("Failed to inject field '" + field.getName()
                        + "' in bean '" + definition.name() + "'", e);
            }
        }
        return bean;
    }

    private void injectAutowired(Object bean, Field field) throws Exception {
        field.setAccessible(true);
        Qualifier qualifier = field.getAnnotation(Qualifier.class);
        Object value;
        if (qualifier != null) {
            value = beanContainer.getBean(qualifier.value(), field.getType());
        } else {
            value = beanContainer.getBean(field.getType());
        }
        field.set(bean, value);
    }

    private void injectValue(Object bean, Field field) throws Exception {
        Value valueAnnotation = field.getAnnotation(Value.class);
        String expression = valueAnnotation.value();
        String resolved = resolveValueExpression(expression, field.getType());
        if (resolved != null) {
            field.setAccessible(true);
            field.set(bean, convertToFieldType(resolved, field.getType()));
        }
    }

    private String resolveValueExpression(String expression, Class<?> fieldType) {
        // Support ${key} and ${key:defaultValue}
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String inner = expression.substring(2, expression.length() - 1);
            int colonIdx = inner.indexOf(':');
            if (colonIdx >= 0) {
                String key = inner.substring(0, colonIdx);
                String defaultValue = inner.substring(colonIdx + 1);
                return environment.getProperty(key, defaultValue);
            } else {
                return environment.getProperty(inner);
            }
        }
        return expression;
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToFieldType(String value, Class<T> type) {
        if (type == String.class) return (T) value;
        if (type == int.class || type == Integer.class) return (T) Integer.valueOf(value);
        if (type == long.class || type == Long.class) return (T) Long.valueOf(value);
        if (type == double.class || type == Double.class) return (T) Double.valueOf(value);
        if (type == float.class || type == Float.class) return (T) Float.valueOf(value);
        if (type == boolean.class || type == Boolean.class) return (T) Boolean.valueOf(value);
        if (type == short.class || type == Short.class) return (T) Short.valueOf(value);
        if (type == byte.class || type == Byte.class) return (T) Byte.valueOf(value);
        return (T) value;
    }

    private Field[] getAllFields(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) return new Field[0];
        Field[] superFields = getAllFields(clazz.getSuperclass());
        Field[] ownFields = clazz.getDeclaredFields();
        Field[] all = new Field[superFields.length + ownFields.length];
        System.arraycopy(superFields, 0, all, 0, superFields.length);
        System.arraycopy(ownFields, 0, all, superFields.length, ownFields.length);
        return all;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 100;
    }
}
