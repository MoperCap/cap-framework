package org.moper.cap.boot.runner;

import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Autowired;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.bean.annotation.Qualifier;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 遍历容器中已注册的 BeanDefinition，为构造函数实例化的 Bean 确定构造函数策略，
 * 并解析构造函数参数的 Bean 名称。
 *
 * <p>执行顺序为 305，在 {@link ComponentBeanRegisterBootstrapRunner}（300）之后、
 * {@link ConfigurationBeanRegisterBootstrapRunner}（310）之前执行。
 *
 * <p><b>构造函数选择规则（按优先级）：</b>
 * <ol>
 *   <li>若存在多个 {@link Autowired} 或 {@link Inject} 标注的构造函数，则抛出异常</li>
 *   <li>若恰好有一个构造函数标注了 {@link Autowired} 或 {@link Inject}，则使用该构造函数</li>
 *   <li>若没有带注解的构造函数但只有一个构造函数，则使用该唯一构造函数</li>
 *   <li>若没有带注解的构造函数且存在多个构造函数，则抛出异常</li>
 * </ol>
 *
 * <p><b>参数 Bean 名称解析规则（按优先级）：</b>
 * <ol>
 *   <li>{@link Qualifier#value()} 显式指定的 Bean 名称</li>
 *   <li>{@link Inject#value()} 显式指定的 Bean 名称（向后兼容）</li>
 *   <li>参数类型的简单类名首字母小写（如 {@code DataSource → dataSource}）</li>
 * </ol>
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 305,
        description = "Determine constructor instantiation strategy for @Component beans")
public class ComponentBeanInstantiationBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer container = context.getBeanContainer();
        String[] beanNames = container.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition def = container.getBeanDefinition(beanName);

            // Only process constructor-instantiated beans without parameters already set
            if (def.isFactoryMethod()) {
                continue;
            }
            if (def.parameterBeanNames().length > 0) {
                continue;
            }

            Class<?> beanType = def.type();
            Constructor<?>[] constructors = beanType.getDeclaredConstructors();

            if (constructors.length == 0) {
                continue;
            }

            Constructor<?> selectedConstructor = selectConstructor(beanType, constructors);

            if (selectedConstructor == null || selectedConstructor.getParameterCount() == 0) {
                // No-arg constructor or no constructor found – nothing to resolve
                log.debug("Bean '{}' uses no-arg constructor", beanName);
                continue;
            }

            String[] paramBeanNames = resolveConstructorParameters(selectedConstructor);
            BeanDefinition updated = def.withParameterBeanNames(paramBeanNames);
            container.removeBeanDefinition(beanName);
            container.registerBeanDefinition(updated);
            log.info("Bean '{}' constructor resolved: {} parameter(s)", beanName, paramBeanNames.length);
        }
    }

    /**
     * 从候选构造函数中选择要使用的构造函数。
     *
     * @param beanType     Bean 类型
     * @param constructors 所有构造函数
     * @return 选中的构造函数，若为无参构造则返回 null
     * @throws BeanDefinitionException 若存在多个 {@code @Autowired}/{@code @Inject} 构造函数，
     *                                  或存在多个构造函数但没有注解标注
     */
    @SuppressWarnings("deprecation")
    private Constructor<?> selectConstructor(Class<?> beanType, Constructor<?>[] constructors) {
        List<Constructor<?>> autowiredConstructors = new ArrayList<>();
        for (Constructor<?> ctor : constructors) {
            if (ctor.isAnnotationPresent(Autowired.class) || ctor.isAnnotationPresent(Inject.class)) {
                autowiredConstructors.add(ctor);
            }
        }

        if (autowiredConstructors.size() > 1) {
            throw new BeanDefinitionException(
                    "Multiple constructors annotated with @Autowired/@Inject found in class: "
                    + beanType.getName());
        }

        if (autowiredConstructors.size() == 1) {
            return autowiredConstructors.get(0);
        }

        if (constructors.length == 1) {
            return constructors[0];
        }

        // Multiple constructors without @Autowired – cannot determine which to use
        throw new BeanDefinitionException(
                "Multiple constructors found in class without @Autowired annotation: "
                + beanType.getName()
                + ". Please annotate the desired constructor with @Autowired.");
    }

    /**
     * 解析构造函数参数对应的 Bean 名称数组。
     *
     * @param constructor 构造函数
     * @return 参数 Bean 名称数组，顺序与参数顺序一致
     */
    @SuppressWarnings("deprecation")
    private static String[] resolveConstructorParameters(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        String[] beanNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            beanNames[i] = resolveParameterBeanName(parameters[i]);
        }
        return beanNames;
    }

    /**
     * 解析单个参数对应的 Bean 名称。
     *
     * <p>解析优先级：
     * <ol>
     *   <li>{@link Qualifier#value()} 显式指定</li>
     *   <li>{@link Inject#value()} 显式指定（向后兼容）</li>
     *   <li>参数类型简单类名首字母小写</li>
     * </ol>
     */
    @SuppressWarnings("deprecation")
    private static String resolveParameterBeanName(Parameter parameter) {
        // Priority 1: @Qualifier
        Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
        if (qualifier != null && !qualifier.value().isBlank()) {
            return qualifier.value();
        }
        // Priority 2: @Inject (backward compatibility)
        Inject inject = parameter.getAnnotation(Inject.class);
        if (inject != null && !inject.value().isBlank()) {
            return inject.value();
        }
        // Priority 3: type name decapitalized
        return Introspector.decapitalize(parameter.getType().getSimpleName());
    }
}
