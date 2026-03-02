package org.moper.cap.boot.interceptor;

import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.property.annotation.Subscriber;
import org.moper.cap.property.annotation.Subscription;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.impl.DefaultAbstractPropertySubscriber;
import org.moper.cap.property.subscriber.impl.DefaultPropertySubscription;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link Subscriber @Subscriber} 字段订阅拦截器。
 *
 * <p>在属性注入完成后扫描 Bean 类中所有标注了 {@link Subscriber} 的字段，
 * 为每个字段创建 {@link PropertySubscriber} 实例，并将聚合的
 * {@link DefaultPropertySubscription} 注册到 {@link PropertyOfficer}。
 *
 * <p>只处理同时标注了 {@link Capper} 和 {@link Subscription} 的类，
 * 以及工厂方法上标注了 {@link Subscription} 的工厂 Bean。
 */
public class PropertySubscriptionBeanInterceptor implements BeanInterceptor {

    private final PropertyOfficer propertyOfficer;
    private final BeanContainer beanContainer;

    public PropertySubscriptionBeanInterceptor(PropertyOfficer propertyOfficer, BeanContainer beanContainer) {
        this.propertyOfficer = propertyOfficer;
        this.beanContainer = beanContainer;
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        Class<?> beanType = definition.type();

        if (!shouldProcess(definition, beanType)) {
            return bean;
        }

        List<PropertySubscriber<?>> subscribers = new ArrayList<>();
        for (Field field : getAllFields(beanType)) {
            Subscriber subscriberAnnotation = field.getAnnotation(Subscriber.class);
            if (subscriberAnnotation == null) continue;
            subscribers.add(createSubscriber(bean, beanType, field, subscriberAnnotation));
        }

        if (subscribers.isEmpty()) {
            return bean;
        }

        String subscriptionName = resolveSubscriptionName(definition, beanType);
        propertyOfficer.getSubscription(subscriptionName,
                () -> new DefaultPropertySubscription(subscriptionName, subscribers));

        return bean;
    }

    private boolean shouldProcess(BeanDefinition definition, Class<?> beanType) {
        if (definition.isFactoryMethod()) {
            return factoryMethodHasSubscription(definition);
        }
        return beanType.isAnnotationPresent(Capper.class)
                && beanType.isAnnotationPresent(Subscription.class);
    }

    private boolean factoryMethodHasSubscription(BeanDefinition definition) {
        BeanDefinition factoryDef = beanContainer.getBeanDefinition(definition.factoryBeanName());
        Class<?> factoryClass = factoryDef.type();
        for (Method method : factoryClass.getDeclaredMethods()) {
            if (method.getName().equals(definition.factoryMethodName())
                    && method.isAnnotationPresent(Subscription.class)) {
                return true;
            }
        }
        return false;
    }

    private String resolveSubscriptionName(BeanDefinition definition, Class<?> beanType) {
        if (definition.isFactoryMethod()) {
            BeanDefinition factoryDef = beanContainer.getBeanDefinition(definition.factoryBeanName());
            Class<?> factoryClass = factoryDef.type();
            for (Method method : factoryClass.getDeclaredMethods()) {
                if (method.getName().equals(definition.factoryMethodName())) {
                    Subscription subscription = method.getAnnotation(Subscription.class);
                    if (subscription != null && !subscription.value().isBlank()) {
                        return subscription.value();
                    }
                }
            }
        } else {
            Subscription subscription = beanType.getAnnotation(Subscription.class);
            if (subscription != null && !subscription.value().isBlank()) {
                return subscription.value();
            }
        }
        return definition.name();
    }

    @SuppressWarnings("unchecked")
    private <T> PropertySubscriber<T> createSubscriber(
            Object bean, Class<?> beanType, Field field, Subscriber annotation) {

        String propertyKey = annotation.propertyKey();
        if (propertyKey == null || propertyKey.isBlank()) {
            throw new BeanException(
                    "@Subscriber.propertyKey must not be blank on field '"
                    + field.getName() + "' in " + beanType.getName());
        }

        Class<T> fieldType = (Class<T>) field.getType();

        Method onSetMethod = resolveOnSetMethod(beanType, field, annotation.onSet(), fieldType);
        Method onRemovedMethod = resolveOnRemovedMethod(beanType, field, annotation.onRemoved());

        field.setAccessible(true);
        if (onSetMethod != null) onSetMethod.setAccessible(true);
        if (onRemovedMethod != null) onRemovedMethod.setAccessible(true);

        final Method finalOnSet = onSetMethod;
        final Method finalOnRemoved = onRemovedMethod;

        return new DefaultAbstractPropertySubscriber<T>(propertyKey, fieldType) {
            @Override
            public void onSet(T value) {
                try {
                    field.set(bean, value);
                    if (finalOnSet != null) finalOnSet.invoke(bean, value);
                } catch (Exception e) {
                    throw new BeanException(
                            "Failed to execute onSet for @Subscriber field '"
                            + field.getName() + "': " + e.getMessage(), e);
                }
            }

            @Override
            public void onRemoved() {
                try {
                    if (finalOnRemoved != null) finalOnRemoved.invoke(bean);
                } catch (Exception e) {
                    throw new BeanException(
                            "Failed to execute onRemoved for @Subscriber field '"
                            + field.getName() + "': " + e.getMessage(), e);
                }
            }
        };
    }

    private Method resolveOnSetMethod(Class<?> beanType, Field field, String methodName, Class<?> fieldType) {
        if (methodName == null || methodName.isBlank()) {
            return null;
        }
        try {
            Method method = beanType.getDeclaredMethod(methodName, fieldType);
            if (!method.getReturnType().equals(void.class)) {
                throw new BeanException(
                        "@Subscriber onSet method '" + methodName + "' in " + beanType.getName()
                        + " must have void return type");
            }
            return method;
        } catch (NoSuchMethodException e) {
            throw new BeanException(
                    "@Subscriber onSet method '" + methodName + "(" + fieldType.getSimpleName() + ")"
                    + "' not found in " + beanType.getName()
                    + " for field '" + field.getName() + "'", e);
        }
    }

    private Method resolveOnRemovedMethod(Class<?> beanType, Field field, String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return null;
        }
        try {
            Method method = beanType.getDeclaredMethod(methodName);
            if (!method.getReturnType().equals(void.class)) {
                throw new BeanException(
                        "@Subscriber onRemoved method '" + methodName + "' in " + beanType.getName()
                        + " must have void return type");
            }
            return method;
        } catch (NoSuchMethodException e) {
            throw new BeanException(
                    "@Subscriber onRemoved method '" + methodName + "()"
                    + "' not found in " + beanType.getName()
                    + " for field '" + field.getName() + "'", e);
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
        return Integer.MIN_VALUE + 300;
    }
}
