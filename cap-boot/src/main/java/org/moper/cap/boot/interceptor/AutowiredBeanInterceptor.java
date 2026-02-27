package org.moper.cap.boot.interceptor;

import org.moper.cap.bean.annotation.Autowired;
import org.moper.cap.bean.annotation.Qualifier;
import org.moper.cap.property.annotation.Subscriber;
import org.moper.cap.property.annotation.Subscription;
import org.moper.cap.property.annotation.Value;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.officer.PropertyView;
import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.impl.DefaultAbstractPropertySubscriber;
import org.moper.cap.property.subscriber.impl.DefaultPropertySubscription;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class AutowiredBeanInterceptor implements BeanInterceptor {

    private final BeanContainer beanContainer;
    private final PropertyOfficer propertyOfficer;

    public AutowiredBeanInterceptor(BeanContainer beanContainer, PropertyOfficer propertyOfficer) {
        this.beanContainer = beanContainer;
        this.propertyOfficer = propertyOfficer;
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
        for (Field field : getAllFields(beanClass)) {
            if (field.isAnnotationPresent(Value.class)) {
                try {
                    field.setAccessible(true);
                    Value valueAnnotation = field.getAnnotation(Value.class);

                    String expression = valueAnnotation.value();
                    Object resolved = resolveValueExpression(expression, field.getType(), propertyOfficer);
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
    private Object resolveValueExpression(String expression, Class<?> fieldType, PropertyView view) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String inner = expression.substring(2, expression.length() - 1);
            int colonIdx = inner.indexOf(':');
            String key = colonIdx >= 0 ? inner.substring(0, colonIdx) : inner;
            String defaultValue = colonIdx >= 0 ? inner.substring(colonIdx + 1) : null;

            Object raw = view.getRawPropertyValue(key);

            String valueStr = raw != null ? raw.toString() : defaultValue;
            return view.getPropertyValue(valueStr, fieldType);
        }
        // 非表达式，直接按类型赋值
        return view.getPropertyValue(expression, fieldType);
    }

    /** 增强：处理 @Subscription 类 + @Subscriber 字段自动属性订阅和回调 */
    private void handleSubscription(Object bean) {
        Class<?> clazz = bean.getClass();
        if (!clazz.isAnnotationPresent(Subscription.class)) return;

        Subscription subscriptionAnnotation = clazz.getAnnotation(Subscription.class);

        final String subscriptionName = subscriptionAnnotation.name().isEmpty() ? "Subscription-" +clazz.getSimpleName() : subscriptionAnnotation.name();

        List<PropertySubscriber<?>> subscribers = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            Subscriber subscriberAnn = field.getAnnotation(Subscriber.class);
            if (subscriberAnn == null) continue;

            String propertyKey = subscriberAnn.propertyKey();
            String onSetMethod = subscriberAnn.onSet();
            String onRemovedMethod = subscriberAnn.onRemoved();

            PropertySubscriber<?> subscriber = new DefaultAbstractPropertySubscriber(propertyKey, field.getType()) {
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
            propertyOfficer.getSubscription(subscriptionName, () -> new DefaultPropertySubscription(subscriptionName, subscribers));
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