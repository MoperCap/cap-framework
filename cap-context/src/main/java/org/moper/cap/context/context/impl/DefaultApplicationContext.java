package org.moper.cap.context.context.impl;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.*;
import org.moper.cap.context.context.ApplicationContext;
import org.moper.cap.context.context.BootstrapContext;
import org.moper.cap.context.exception.ContextException;
import org.moper.cap.property.officer.PropertyOfficer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link ApplicationContext} 的默认实现 </br>
 * 委托 {@link BeanContainer} 实现 BeanProvider 和 BeanInspector 的所有方法
 */
public class DefaultApplicationContext implements ApplicationContext {

    private static final Logger log = LoggerFactory.getLogger(DefaultApplicationContext.class);

    private final BeanContainer beanContainer;
    private final PropertyOfficer propertyOfficer;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DefaultApplicationContext(BootstrapContext bootstrapContext) {
        this.beanContainer = bootstrapContext.getBeanContainer();
        this.propertyOfficer = bootstrapContext.getPropertyOfficer();
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
    }



    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)){
            return;
        }
        try {
            beanContainer.destroySingletons();
        } catch (BeanDestructionException e) {
            log.error("Error destroying singletons during context close", e);
        }

        try {
            propertyOfficer.close();
        } catch (Exception e) {
            log.error("Error closing property officer", e);
        }
    }

    /**
     * 获取属性管理平台
     */
    @Override
    public PropertyOfficer getPropertyOfficer() {
        return propertyOfficer;
    }

    // ===== BeanProvider delegation =====

    @Override
    public  Object getBean( String beanName)
            throws NoSuchBeanDefinitionException, BeanCreationException {
        return beanContainer.getBean(beanName);
    }

    @Override
    public <T>  T getBean( String beanName,  Class<T> requiredType)
            throws NoSuchBeanDefinitionException, BeanCreationException, BeanNotOfRequiredTypeException {
        return beanContainer.getBean(beanName, requiredType);
    }

    @Override
    public <T>  T getBean( Class<T> requiredType)
            throws NoSuchBeanDefinitionException, BeanCreationException, NoUniqueBeanDefinitionException {
        return beanContainer.getBean(requiredType);
    }

    // ===== BeanInspector delegation =====

    @Override
    public boolean containsBean( String beanName) {
        return beanContainer.containsBean(beanName);
    }

    @Override
    public boolean containsBeanDefinition( String beanName) {
        return beanContainer.containsBeanDefinition(beanName);
    }

    @Override
    public  BeanDefinition getBeanDefinition( String beanName)
            throws NoSuchBeanDefinitionException {
        return beanContainer.getBeanDefinition(beanName);
    }

    @Override
    public  String[] getBeanDefinitionNames() {
        return beanContainer.getBeanDefinitionNames();
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanContainer.getBeanDefinitionCount();
    }

    @Override
    public  String[] getBeanNamesForType( Class<?> type) {
        return beanContainer.getBeanNamesForType(type);
    }

    @Override
    public  String[] getBeanNamesForAnnotation( Class<? extends Annotation> annotationType) {
        return beanContainer.getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public <T>  Map<String, T> getBeansOfType( Class<T> type) throws BeanCreationException {
        return beanContainer.getBeansOfType(type);
    }

    @Override
    public  Map<String, Object> getBeansWithAnnotation(
             Class<? extends Annotation> annotationType) throws BeanCreationException {
        return beanContainer.getBeansWithAnnotation(annotationType);
    }

    @Override
    public boolean isSingleton( String beanName) throws NoSuchBeanDefinitionException {
        return beanContainer.isSingleton(beanName);
    }

    @Override
    public boolean isPrototype( String beanName) throws NoSuchBeanDefinitionException {
        return beanContainer.isPrototype(beanName);
    }

    @Override
    public boolean isTypeMatch( String beanName,  Class<?> targetType)
            throws NoSuchBeanDefinitionException {
        return beanContainer.isTypeMatch(beanName, targetType);
    }

    @Override
    public  Class<?> getType( String beanName) throws NoSuchBeanDefinitionException {
        return beanContainer.getType(beanName);
    }

    @Override
    public  String[] getAliases( String beanName) throws NoSuchBeanDefinitionException {
        return beanContainer.getAliases(beanName);
    }

    @Override
    public <A extends Annotation>  A findAnnotationOnBean(
             String beanName,  Class<A> annotationType)
            throws NoSuchBeanDefinitionException {
        return beanContainer.findAnnotationOnBean(beanName, annotationType);
    }
}
