package org.moper.cap.boot.interceptor;

import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;
import org.moper.cap.bean.util.BeanNamesResolver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Inject 字段注入拦截器。
 *
 * <p>在属性注入阶段扫描 Bean 类中所有标注了 {@link Inject} 的字段，
 * 并从 {@link BeanContainer} 中解析依赖进行注入。
 * 支持 private/protected/public 字段可见性。
 */
public class BeanInjectionInterceptor implements BeanInterceptor {

    private final BeanContainer beanContainer;

    public BeanInjectionInterceptor(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
    }

    @Override
    public Object afterPropertyInjection(Object bean, BeanDefinition definition) throws BeanException {
        injectFields(bean);
        return bean;
    }

    private void injectFields(Object bean) {
        Class<?> beanClass = bean.getClass();
        for (Field field : getAllFields(beanClass)) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            try {
                field.setAccessible(true);
                String beanName = BeanNamesResolver.resolveField(field);
                Object dependency = beanContainer.getBean(beanName, field.getType());
                field.set(bean, dependency);
            } catch (Exception e) {
                throw new BeanException(
                        "Failed to inject @Inject field '" + field.getName() +
                        "' in " + beanClass.getName() + ": " + e.getMessage(), e);
            }
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
        return Integer.MIN_VALUE + 100;
    }
}
