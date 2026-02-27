package org.moper.cap.core.context;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.core.argument.CommandArgumentParser;
import org.moper.cap.core.config.ConfigurationClassParser;
import org.moper.cap.property.officer.PropertyOfficer;

import java.util.function.Function;

/**
 * 框架初始化完成后的系统上下文。
 * 所有初始化工作在 DefaultBootstrapContext 构造时完成。
 * build() 仅负责将已初始化的 BootstrapContext 转换为 ApplicationContext。
 */
public interface BootstrapContext {

    /**
     * 获取已完成 BeanDefinition 注册的 Bean 容器
     */
    BeanContainer getBeanContainer();

    /**
     * 获取属性管理平台
     */
    PropertyOfficer getPropertyOfficer();

    /**
     * 获取命令行参数解析器
     */
    CommandArgumentParser getCommandArgumentParser();

    /**
     * 获取配置类解析器
     */
    ConfigurationClassParser getConfigurationClassParser();

    <T extends RuntimeContext> T build(Function<BootstrapContext, T> factory) throws Exception;
}