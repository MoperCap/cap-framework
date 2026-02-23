package org.moper.cap.boot.interceptor;

import org.moper.cap.boot.annotation.Autowired;
import org.moper.cap.boot.annotation.Qualifier;
import org.moper.cap.boot.annotation.Subscriber;
import org.moper.cap.boot.annotation.Subscription;
import org.moper.cap.boot.annotation.Value;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.environment.Environment;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.subcription.DefaultPropertySubscription;
import org.moper.cap.property.subscriber.PropertyViewPool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class AutowiredBeanInterceptor implements BeanInterceptor {

    private final BeanContainer beanContainer;
    private final Environment environment;

    public AutowiredBeanInterceptor(BeanContainer beanContainer, Environment environment) {
        this.beanContainer = beanContainer;
        this.environment = environment;
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        injectAutowired(bean, definition);
        injectValue(bean, definition);
        handleSubscription(bean);
        return bean;
    }

    /** 支持 @Autowired 和 @Qualifier 注入 */
    private void injectAutowired(Object bean, BeanDefinition definition) {
        Class<?> beanClass = bean.getClass();
        for (Field field : getAllFields(beanClass)) {
            if (field.isAnnotationPresent(Autowired.class)) {
                try {
                    field.setAccessible(true);
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    Object value;
                    if (qualifier != null && !qualifier.value().isEmpty()) {
                        value = beanContainer.getBean(qualifier.value(), field.getType());
                    } else {
                        value = beanContainer.getBean(field.getType());
                    }
                    field.set(bean, value);
                } catch (Exception ignore) {
                    // silent catch for framework event model
                }
            }
        }
    }

    /** 支持 @Value 注入 */
    private void injectValue(Object bean, BeanDefinition definition) {
        Class<?> beanClass = bean.getClass();
        PropertyViewPool viewPool = environment.getViewPool();
        for (Field field : getAllFields(beanClass)) {
            if (field.isAnnotationPresent(Value.class)) {
                try {
                    field.setAccessible(true);
                    Value valueAnnotation = field.getAnnotation(Value.class);

                    String expression = valueAnnotation.value();
                    Object resolved = resolveValueExpression(expression, field.getType(), viewPool);
                    if (resolved != null) {
                        field.set(bean, resolved);
                    }
                } catch (Exception ignore) {
                    // silent catch for event model
                }
            }
        }
    }

    /** 解析 SpEL 格式: ${key} 或 ${key:default} */
    private Object resolveValueExpression(String expression, Class<?> fieldType, PropertyViewPool viewPool) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String inner = expression.substring(2, expression.length() - 1);
            int colonIdx = inner.indexOf(':');
            String key = colonIdx >= 0 ? inner.substring(0, colonIdx) : inner;
            String defaultValue = colonIdx >= 0 ? inner.substring(colonIdx + 1) : null;

            Object raw = viewPool.getRawPropertyValue(key);
            String valueStr = raw != null ? raw.toString() : defaultValue;
            return convertToFieldType(valueStr, fieldType);
        }
        // 非表达式，直接按类型赋值
        return convertToFieldType(expression, fieldType);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToFieldType(String value, Class<T> type) {
        if (value == null) return null;
        try {
            if (type == String.class) return (T) value;
            if (type == int.class || type == Integer.class) return (T) Integer.valueOf(value);
            if (type == long.class || type == Long.class) return (T) Long.valueOf(value);
            if (type == double.class || type == Double.class) return (T) Double.valueOf(value);
            if (type == float.class || type == Float.class) return (T) Float.valueOf(value);
            if (type == boolean.class || type == Boolean.class) return (T) Boolean.valueOf(value);
            if (type == short.class || type == Short.class) return (T) Short.valueOf(value);
            if (type == byte.class || type == Byte.class) return (T) Byte.valueOf(value);
        } catch (Exception ignore) {}
        return null;
    }

    /** 增强：处理 @Subscription 类 + @Subscriber 字段自动属性订阅和回调 */
    private void handleSubscription(Object bean) {
        Class<?> clazz = bean.getClass();
        if (!clazz.isAnnotationPresent(Subscription.class)) return;
        String subscriptionName = clazz.getAnnotation(Subscription.class).value();
        if (subscriptionName.isEmpty()) subscriptionName = clazz.getSimpleName() + "Subscription";

        List<PropertySubscriber> subscribers = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            Subscriber subscriberAnn = field.getAnnotation(Subscriber.class);
            if (subscriberAnn == null) continue;

            String propertyKey = subscriberAnn.propertyKey();
            String onSetMethod = subscriberAnn.onSet();
            String onRemovedMethod = subscriberAnn.onRemoved();

            PropertySubscriber subscriber = new PropertySubscriber() {
                @Override
                public String getPropertyKey() { return propertyKey; }

                @Override
                public void onSet(Object value) {
                    try {
                        field.setAccessible(true);
                        field.set(bean, value);
                        if (!onSetMethod.isEmpty()) {
                            Method m = clazz.getDeclaredMethod(onSetMethod, Object.class);
                            m.setAccessible(true);
                            m.invoke(bean, value);
                        }
                    } catch (Exception ignore) {
                        // silent catch
                    }
                }

                @Override
                public void onRemoved() {
                    try {
                        field.setAccessible(true);
                        field.set(bean, null);
                        if (!onRemovedMethod.isEmpty()) {
                            Method m = clazz.getDeclaredMethod(onRemovedMethod);
                            m.setAccessible(true);
                            m.invoke(bean);
                        }
                    } catch (Exception ignore) {
                        // silent catch
                    }
                }
            };
            subscribers.add(subscriber);
        }

        if (!subscribers.isEmpty()) {
            DefaultPropertySubscription subscription =
                    new DefaultPropertySubscription(subscriptionName, subscribers);
            PropertyOfficer officer = environment.getOfficer();
            officer.subscribe(subscription);
        }
    }

    /** 获取所有字段（包括父类） */
    private List<Field> getAllFields(Class<?> beanClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = beanClass;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}