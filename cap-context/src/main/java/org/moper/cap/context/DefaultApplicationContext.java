package org.moper.cap.context;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.*;
import org.moper.cap.environment.Environment;
import org.moper.cap.event.ApplicationEvent;
import org.moper.cap.event.ApplicationListener;
import org.moper.cap.event.ContextClosedEvent;
import org.moper.cap.event.ContextStartedEvent;
import org.moper.cap.exception.ContextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link ApplicationContext} 的默认实现 </br>
 * 委托 {@link BeanContainer} 实现 BeanProvider 和 BeanInspector 的所有方法
 */
public class DefaultApplicationContext implements ApplicationContext {

    private static final Logger log = LoggerFactory.getLogger(DefaultApplicationContext.class);

    private final BeanContainer beanContainer;
    private final Environment environment;
    private final List<ApplicationListener<ApplicationEvent>> listeners = new ArrayList<>();

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DefaultApplicationContext(BootstrapContext bootstrapContext) {
        this.beanContainer = bootstrapContext.getBeanContainer();
        this.environment = bootstrapContext.getEnvironment();
    }

    @Override
    public void run() throws ContextException {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            beanContainer.preInstantiateSingletons();
        } catch (BeanCreationException e) {
            throw new ContextException("Failed to pre-instantiate singletons", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        publishEvent(new ContextStartedEvent(this));
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        publishEvent(new ContextClosedEvent(this));
        try {
            beanContainer.destroySingletons();
        } catch (BeanDestructionException e) {
            log.error("Error destroying singletons during context close", e);
        }
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void publishEvent(ApplicationEvent event) {
        for (ApplicationListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("Error in event listener for event {}", event.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 注册应用事件监听器
     *
     * @param listener 监听器
     */
    @SuppressWarnings("unchecked")
    public <E extends ApplicationEvent> void addApplicationListener(ApplicationListener<E> listener) {
        listeners.add((ApplicationListener<ApplicationEvent>) listener);
    }

    // ===== BeanProvider delegation =====

    @Override
    public @NotNull Object getBean(@NotBlank String beanName)
            throws NoSuchBeanDefinitionException, BeanCreationException {
        return beanContainer.getBean(beanName);
    }

    @Override
    public <T> @NotNull T getBean(@NotBlank String beanName, @NotNull Class<T> requiredType)
            throws NoSuchBeanDefinitionException, BeanCreationException, BeanNotOfRequiredTypeException {
        return beanContainer.getBean(beanName, requiredType);
    }

    @Override
    public <T> @NotNull T getBean(@NotNull Class<T> requiredType)
            throws NoSuchBeanDefinitionException, BeanCreationException, NoUniqueBeanDefinitionException {
        return beanContainer.getBean(requiredType);
    }

    // ===== BeanInspector delegation =====

    @Override
    public boolean containsBean(@NotBlank String beanName) {
        return beanContainer.containsBean(beanName);
    }

    @Override
    public boolean containsBeanDefinition(@NotBlank String beanName) {
        return beanContainer.containsBeanDefinition(beanName);
    }

    @Override
    public @NotNull BeanDefinition getBeanDefinition(@NotBlank String beanName)
            throws NoSuchBeanDefinitionException {
        return beanContainer.getBeanDefinition(beanName);
    }

    @Override
    public @NotNull String[] getBeanDefinitionNames() {
        return beanContainer.getBeanDefinitionNames();
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanContainer.getBeanDefinitionCount();
    }

    @Override
    public @NotNull String[] getBeanNamesForType(@NotNull Class<?> type) {
        return beanContainer.getBeanNamesForType(type);
    }

    @Override
    public @NotNull String[] getBeanNamesForAnnotation(@NotNull Class<? extends Annotation> annotationType) {
        return beanContainer.getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public <T> @NotNull Map<String, T> getBeansOfType(@NotNull Class<T> type) throws BeanCreationException {
        return beanContainer.getBeansOfType(type);
    }

    @Override
    public @NotNull Map<String, Object> getBeansWithAnnotation(
            @NotNull Class<? extends Annotation> annotationType) throws BeanCreationException {
        return beanContainer.getBeansWithAnnotation(annotationType);
    }

    @Override
    public boolean isSingleton(@NotBlank String beanName) throws NoSuchBeanDefinitionException {
        return beanContainer.isSingleton(beanName);
    }

    @Override
    public boolean isPrototype(@NotBlank String beanName) throws NoSuchBeanDefinitionException {
        return beanContainer.isPrototype(beanName);
    }

    @Override
    public boolean isTypeMatch(@NotBlank String beanName, @NotNull Class<?> targetType)
            throws NoSuchBeanDefinitionException {
        return beanContainer.isTypeMatch(beanName, targetType);
    }

    @Override
    public @NotNull Class<?> getType(@NotBlank String beanName) throws NoSuchBeanDefinitionException {
        return beanContainer.getType(beanName);
    }

    @Override
    public @NotNull String[] getAliases(@NotBlank String beanName) throws NoSuchBeanDefinitionException {
        return beanContainer.getAliases(beanName);
    }

    @Override
    public <A extends Annotation> @Nullable A findAnnotationOnBean(
            @NotBlank String beanName, @NotNull Class<A> annotationType)
            throws NoSuchBeanDefinitionException {
        return beanContainer.findAnnotationOnBean(beanName, annotationType);
    }
}
