package org.moper.cap.context.context.impl;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.container.impl.DefaultBeanContainer;
import org.moper.cap.context.config.ConfigurationClass;
import org.moper.cap.context.config.impl.DefaultConfigurationClass;
import org.moper.cap.context.context.RuntimeContext;
import org.moper.cap.context.context.BootstrapContext;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.officer.impl.DefaultPropertyOfficer;

import java.util.function.Function;

public class DefaultBootstrapContext implements BootstrapContext {

    private final BeanContainer beanContainer;

    private final PropertyOfficer propertyOfficer;

    private final ConfigurationClass configurationClass;

    public DefaultBootstrapContext(Class<?> configClass) {
        this.beanContainer = new DefaultBeanContainer();
        this.propertyOfficer = new DefaultPropertyOfficer("cap-property-officer");
        this.configurationClass = new DefaultConfigurationClass(configClass);
    }

    /**
     * 获取已完成 BeanDefinition 注册的 Bean 容器
     */
    @Override
    public BeanContainer getBeanContainer() {
        return beanContainer;
    }

    /**
     * 获取属性管理平台
     */
    @Override
    public PropertyOfficer getPropertyOfficer() {
        return propertyOfficer;
    }

    /**
     * 获取配置类信息视图
     */
    @Override
    public ConfigurationClass getConfigurationClass() {
        return configurationClass;
    }

    public RuntimeContext build(){
        return new DefaultRuntimeContext(this);
    }

    @Override
    public <T extends RuntimeContext> T build(Function<BootstrapContext, T> factory) throws Exception {
        return factory.apply(this);
    }

}
