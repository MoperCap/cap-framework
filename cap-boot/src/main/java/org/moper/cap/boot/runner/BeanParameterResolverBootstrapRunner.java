package org.moper.cap.boot.runner;

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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 解析工厂方法（{@code @Bean} / {@code @Capper} 方法）所对应的 BeanDefinition 的参数依赖。
 *
 * <p>执行顺序为 315，在 {@link ConfigurationBeanRegisterBootstrapRunner}（310）之后、
 * {@link BeanLifecycleBootstrapRunner}（325）之前执行。
 *
 * <p>该 Runner 遍历容器中所有通过工厂方法实例化的 BeanDefinition（即 {@code isFactoryMethod()}
 * 为 {@code true} 且 {@code parameterBeanNames} 为空），解析其工厂方法的参数 Bean 名称，
 * 并更新 BeanDefinition。
 *
 * <p><b>参数 Bean 名称解析规则（按优先级）：</b>
 * <ol>
 *   <li>{@link Qualifier#value()} 显式指定的 Bean 名称</li>
 *   <li>{@link Inject#value()} 显式指定的 Bean 名称（向后兼容）</li>
 *   <li>参数类型的简单类名首字母小写（如 {@code DataSource → dataSource}）</li>
 * </ol>
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 315,
        description = "Resolve factory method parameter dependencies for @Bean / @Capper beans")
public class BeanParameterResolverBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer container = context.getBeanContainer();
        String[] beanNames = container.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            BeanDefinition def = container.getBeanDefinition(beanName);

            // Only process factory-method beans without parameters already set
            if (!def.isFactoryMethod()) {
                continue;
            }
            if (def.parameterBeanNames().length > 0) {
                continue;
            }

            String factoryBeanName = def.factoryBeanName();
            String factoryMethodName = def.factoryMethodName();
            BeanDefinition factoryDef = container.getBeanDefinition(factoryBeanName);
            Class<?> factoryClass = factoryDef.type();

            Method factoryMethod = findFactoryMethod(factoryClass, factoryMethodName, def.type());
            if (factoryMethod == null) {
                throw new BeanDefinitionException(
                        "Cannot find factory method '" + factoryMethodName + "' in class '"
                        + factoryClass.getName() + "' for bean '" + beanName + "'");
            }

            if (factoryMethod.getParameterCount() == 0) {
                continue;
            }

            String[] paramBeanNames = resolveMethodParameters(factoryMethod);
            BeanDefinition updated = def.withParameterBeanNames(paramBeanNames);
            container.removeBeanDefinition(beanName);
            container.registerBeanDefinition(updated);
            log.info("Bean '{}' factory method parameters resolved: {} parameter(s)",
                    beanName, paramBeanNames.length);
        }
    }

    /**
     * 在指定类中查找与给定名称和返回类型匹配的工厂方法。
     *
     * @param factoryClass      工厂类
     * @param factoryMethodName 工厂方法名称
     * @param returnType        工厂方法返回类型（Bean 类型）
     * @return 找到的方法，或 {@code null}
     */
    private Method findFactoryMethod(Class<?> factoryClass, String factoryMethodName, Class<?> returnType) {
        for (Method method : factoryClass.getDeclaredMethods()) {
            if (method.getName().equals(factoryMethodName)
                    && returnType.isAssignableFrom(method.getReturnType())) {
                return method;
            }
        }
        return null;
    }

    /**
     * 解析工厂方法参数对应的 Bean 名称数组。
     *
     * @param method 工厂方法
     * @return 参数 Bean 名称数组，顺序与参数顺序一致
     */
    @SuppressWarnings("deprecation")
    private static String[] resolveMethodParameters(Method method) {
        Parameter[] parameters = method.getParameters();
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
        Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
        if (qualifier != null && !qualifier.value().isBlank()) {
            return qualifier.value();
        }
        Inject inject = parameter.getAnnotation(Inject.class);
        if (inject != null && !inject.value().isBlank()) {
            return inject.value();
        }
        return Introspector.decapitalize(parameter.getType().getSimpleName());
    }
}
