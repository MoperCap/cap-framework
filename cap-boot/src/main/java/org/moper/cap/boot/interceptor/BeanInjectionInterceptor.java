package org.moper.cap.boot.interceptor;

import org.moper.cap.bean.annotation.Autowired;
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
 * {@link Autowired} / {@link Inject} 字段注入拦截器。
 *
 * <p>在属性注入阶段扫描 Bean 类中所有标注了 {@link Autowired} 或 {@link Inject} 的字段，
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

    @SuppressWarnings("deprecation")
    private void injectFields(Object bean) {
        Class<?> beanClass = bean.getClass();
        for (Field field : getAllFields(beanClass)) {
            boolean hasAutowired = field.isAnnotationPresent(Autowired.class);
            boolean hasInject = field.isAnnotationPresent(Inject.class);
            if (!hasAutowired && !hasInject) {
                continue;
            }
            try {
                field.setAccessible(true);
                String beanName = resolveFieldBeanName(field);
                boolean required = !hasAutowired || field.getAnnotation(Autowired.class).required();
                if (!required && !beanContainer.containsBean(beanName)) {
                    continue;
                }
                Object dependency = beanContainer.getBean(beanName, field.getType());
                field.set(bean, dependency);
            } catch (Exception e) {
                throw new BeanException(
                        "Failed to inject field '" + field.getName() +
                        "' in " + beanClass.getName() + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * 解析字段对应的 Bean 名称。
     *
     * <p>解析优先级：
     * <ol>
     *   <li>{@link org.moper.cap.bean.annotation.Qualifier#value()} 显式指定</li>
     *   <li>{@link Inject#value()} 显式指定（向后兼容）</li>
     *   <li>字段类型简单类名首字母小写</li>
     * </ol>
     */
    @SuppressWarnings("deprecation")
    private String resolveFieldBeanName(Field field) {
        org.moper.cap.bean.annotation.Qualifier qualifier =
                field.getAnnotation(org.moper.cap.bean.annotation.Qualifier.class);
        if (qualifier != null && !qualifier.value().isBlank()) {
            return qualifier.value();
        }
        Inject inject = field.getAnnotation(Inject.class);
        if (inject != null && !inject.value().isBlank()) {
            return inject.value();
        }
        return BeanNamesResolver.resolve(field);
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
        return 350;
    }
}
