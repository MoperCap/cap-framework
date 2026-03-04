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
import org.moper.cap.core.util.AnnotationMutualExclusivity;
import org.moper.cap.core.util.AnnotationUtils;

import java.beans.Introspector;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Scans {@link Component}-annotated classes (including meta-annotation aliases such as
 * {@code @Controller} and {@code @Service}) and {@link Capper}-annotated classes, then
 * registers a {@link BeanDefinition} for each one (type and basic attributes only; constructor
 * parameters are resolved later by {@link ComponentBeanInstantiationBootstrapRunner}).
 *
 * <p>Execution order is 300, before {@link ComponentBeanInstantiationBootstrapRunner} (305).
 *
 * <p>Before registering a component class the runner validates it with
 * {@link AnnotationMutualExclusivity} to ensure no class carries more than one
 * {@code @Component} semantic annotation.  Bean names are resolved via
 * {@link AnnotationUtils#resolveComponentBeanName(Class)} which follows any
 * {@code @AliasFor} bridge back to {@code @Component.value()}.
 *
 * <p>Legacy {@link Capper} support is retained for backward compatibility.
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

            // Scan @Component classes (includes meta-annotation aliases via ClassGraph)
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

        // Validate: only one @Component semantic annotation per class
        AnnotationMutualExclusivity.validate(clazz);

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
     * Resolves the bean name(s) for a {@link Component}-annotated class.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>{@link Configuration#value()} (cap-oguri) if present and non-blank</li>
     *   <li>{@link AnnotationUtils#resolveComponentBeanName(Class)} which follows any
     *       {@code @AliasFor} bridge to {@code @Component.value()} – covers direct
     *       {@code @Component} usage and meta-annotation aliases like {@code @Controller}</li>
     *   <li>Simple class name, first letter lower-cased</li>
     * </ol>
     */
    private static String[] resolveComponentBeanNames(Class<?> clazz) {
        String defaultName = Introspector.decapitalize(clazz.getSimpleName());

        // @Configuration (cap-oguri) takes precedence – its value() has no @AliasFor
        Configuration configuration = clazz.getAnnotation(Configuration.class);
        if (configuration != null && !configuration.value().isBlank()) {
            return new String[]{configuration.value()};
        }

        // Resolve via @AliasFor chains (covers @Component.value and aliases like @Controller)
        String aliasedName = AnnotationUtils.resolveComponentBeanName(clazz);
        if (!aliasedName.isBlank()) {
            return new String[]{aliasedName};
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
