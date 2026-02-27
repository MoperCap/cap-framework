package org.moper.cap.core.context.impl;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.core.argument.CommandArgumentParser;
import org.moper.cap.core.config.ConfigurationClassParser;
import org.moper.cap.core.context.RuntimeContext;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.property.officer.PropertyOfficer;

import java.util.function.Function;

public class DefaultBootstrapContext implements BootstrapContext {

    private final BeanContainer beanContainer;

    private final PropertyOfficer propertyOfficer;

    private final CommandArgumentParser commandArgumentParser;

    private final ConfigurationClassParser configurationClassParser;

    public DefaultBootstrapContext(BeanContainer beanContainer, PropertyOfficer propertyOfficer, CommandArgumentParser commandArgumentParser, ConfigurationClassParser configurationClassParser) {
        if(beanContainer == null){
            throw new IllegalArgumentException("beanContainer cannot be null");
        }

        if(propertyOfficer == null){
            throw new IllegalArgumentException("propertyOfficer cannot be null");
        }

        if(commandArgumentParser == null){
            throw new IllegalArgumentException("commandArgumentParser cannot be null");
        }

        if(configurationClassParser == null){
            throw new IllegalArgumentException("configurationClassParser cannot be null");
        }

        this.beanContainer = beanContainer;
        this.propertyOfficer = propertyOfficer;
        this.commandArgumentParser = commandArgumentParser;
        this.configurationClassParser = configurationClassParser;
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
     * 获取命令行参数解析器
     */
    @Override
    public CommandArgumentParser getCommandArgumentParser() {
        return commandArgumentParser;
    }

    /**
     * 获取配置类信息视图
     */
    @Override
    public ConfigurationClassParser getConfigurationClassParser() {
        return configurationClassParser;
    }

    public RuntimeContext build(){
        return new DefaultRuntimeContext(this);
    }

    @Override
    public <T extends RuntimeContext> T build(Function<BootstrapContext, T> factory) throws Exception {
        return factory.apply(this);
    }

}
