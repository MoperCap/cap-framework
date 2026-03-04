package org.moper.cap.boot.runner;

import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Component;
import org.moper.cap.bean.annotation.Configuration;
import org.moper.cap.bean.annotation.Lazy;
import org.moper.cap.bean.annotation.Primary;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.beans.Introspector;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 扫描 {@link Component}（含 {@link Configuration}）和 {@link Capper} 标注的类，
 * 并将其注册为 BeanDefinition（仅注册类型和基础属性，不处理构造函数参数）。
 *
 * <p>执行顺序为 300，在 {@link ComponentBeanInstantiationBootstrapRunner}（305）之前执行。
 * 构造函数参数的解析由后续的 {@link ComponentBeanInstantiationBootstrapRunner} 负责。
 *
 * <p>同时处理 {@link Capper} 注解（已废弃），以保持向后兼容。
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 300,
        description = "Scan @Component/@Configuration annotated classes and register BeanDefinitions")
public class ComponentBeanRegisterBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer container = context.getBeanContainer();

        try (ScanResult scan = new ClassGraph().enableAllInfo()
                .acceptPackages(context.getConfigurationClassParser().getComponentScanPaths())
                .scan()) {

            // Scan @Component classes (includes @Configuration via meta-annotation)
            for (ClassInfo classInfo : scan.getClassesWithAnnotation(Component.class)
                    .filter(ci -> !ci.isInterface() && !ci.isAbstract() && !ci.isAnnotation())) {

                registerComponentClass(container, classInfo);
            }

            // Scan @Capper annotated classes (deprecated, for backward compatibility)
            for (ClassInfo classInfo : scan.getClassesWithAnnotation(Capper.class)
                    .filter(ci -> !ci.isInterface() && !ci.isAbstract() && !ci.isAnnotation())) {

                registerCapperClass(container, classInfo);
            }
        }
    }

    private void registerComponentClass(BeanContainer container, ClassInfo classInfo) {
        Class<?> clazz = classInfo.loadClass();

        String[] beanNames = resolveComponentBeanNames(clazz);
        String primaryBeanName = beanNames[0];

        // Skip if already registered (e.g., by a prior iteration for @Capper)
        if (container.containsBeanDefinition(primaryBeanName)) {
            return;
        }

        boolean isPrimary = clazz.isAnnotationPresent(Primary.class);
        boolean isLazy = clazz.isAnnotationPresent(Lazy.class);

        BeanDefinition def = BeanDefinition.of(primaryBeanName, clazz)
                .withPrimary(isPrimary)
                .withLazy(isLazy);
        container.registerBeanDefinition(def);
        log.info("Register bean: {}", def);

        // Register aliases
        for (int i = 1; i < beanNames.length; i++) {
            String alias = beanNames[i];
            container.registerAlias(primaryBeanName, alias);
            log.info("Register alias: {} -> {}", alias, primaryBeanName);
        }
    }

    @SuppressWarnings("deprecation")
    private void registerCapperClass(BeanContainer container, ClassInfo classInfo) {
        Class<?> clazz = classInfo.loadClass();

        // If already handled as a @Component, skip
        if (clazz.isAnnotationPresent(Component.class)) {
            return;
        }

        Capper capper = clazz.getAnnotation(Capper.class);
        String[] beanNames = resolveCapperBeanNames(clazz, capper);
        String primaryBeanName = beanNames[0];

        if (container.containsBeanDefinition(primaryBeanName)) {
            return;
        }

        BeanDefinition def = BeanDefinition.of(primaryBeanName, clazz)
                .withPrimary(capper.primary())
                .withLazy(capper.lazy())
                .withScope(capper.scope())
                .withDescription(capper.description());
        container.registerBeanDefinition(def);
        log.info("Register bean (legacy @Capper): {}", def);

        for (int i = 1; i < beanNames.length; i++) {
            String alias = beanNames[i];
            container.registerAlias(primaryBeanName, alias);
            log.info("Register alias: {} -> {}", alias, primaryBeanName);
        }
    }

    /**
     * 解析 {@link Component} 或 {@link Configuration} 注解中的 Bean 名称。
     *
     * <p>解析规则：
     * <ol>
     *   <li>优先读取 {@link Configuration#value()} 或 {@link Component#value()} 中的显式名称</li>
     *   <li>若未指定，使用类名首字母小写作为默认 Bean 名称</li>
     * </ol>
     */
    private static String[] resolveComponentBeanNames(Class<?> clazz) {
        String defaultName = Introspector.decapitalize(clazz.getSimpleName());

        // @Configuration takes precedence over @Component since it's a meta-annotation
        Configuration configuration = clazz.getAnnotation(Configuration.class);
        if (configuration != null && !configuration.value().isBlank()) {
            return new String[]{configuration.value()};
        }

        Component component = clazz.getAnnotation(Component.class);
        if (component != null && !component.value().isBlank()) {
            return new String[]{component.value()};
        }

        return new String[]{defaultName};
    }

    @SuppressWarnings("deprecation")
    private static String[] resolveCapperBeanNames(Class<?> clazz, Capper capper) {
        String defaultName = Introspector.decapitalize(clazz.getSimpleName());
        if (capper == null) {
            return new String[]{defaultName};
        }
        String[] names = capper.names();
        if (names == null || names.length == 0) {
            return new String[]{defaultName};
        }
        Set<String> resultSet = new LinkedHashSet<>();
        for (String n : names) {
            if (n != null && !n.isBlank()) {
                resultSet.add(n);
            }
        }
        return resultSet.isEmpty() ? new String[]{defaultName} : resultSet.toArray(new String[0]);
    }
}
