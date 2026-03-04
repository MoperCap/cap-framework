package org.moper.cap.boot.runner;

import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Bean;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Configuration;
import org.moper.cap.bean.annotation.Lazy;
import org.moper.cap.bean.annotation.Primary;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 扫描 {@link Configuration} 类中的 {@link Bean} 方法，并将其注册为独立的 BeanDefinition。
 *
 * <p>执行顺序为 310，在 {@link ComponentBeanInstantiationBootstrapRunner}（305）之后、
 * {@link BeanParameterResolverBootstrapRunner}（315）之前执行。
 *
 * <p>该 Runner 遍历容器中所有已注册的 BeanDefinition，找到标注了 {@link Configuration}
 * 的类，再扫描其中所有标注了 {@link Bean} 的方法，为每个方法创建独立的 BeanDefinition。
 *
 * <p>同时处理遗留的 {@link Capper} 方法注解（向后兼容）。
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 310,
        description = "Scan @Bean methods in @Configuration classes and register BeanDefinitions")
public class ConfigurationBeanRegisterBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer container = context.getBeanContainer();

        // Process @Bean methods from @Configuration classes already in container
        String[] existingBeanNames = container.getBeanDefinitionNames();
        for (String configBeanName : existingBeanNames) {
            BeanDefinition configDef = container.getBeanDefinition(configBeanName);
            Class<?> configClass = configDef.type();

            if (configClass.isAnnotationPresent(Configuration.class)) {
                processBeanMethods(container, configBeanName, configClass);
            }
        }

        // Also handle legacy @Capper factory methods on any class (backward compatibility)
        processLegacyCapperMethods(container, context);
    }

    private void processBeanMethods(BeanContainer container, String configBeanName, Class<?> configClass) {
        for (Method method : configClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Bean.class)) {
                continue;
            }

            Bean beanAnnotation = method.getAnnotation(Bean.class);
            String[] beanNames = resolveBeanMethodNames(method, beanAnnotation);
            String primaryBeanName = beanNames[0];

            Class<?> beanType = method.getReturnType();
            boolean isPrimary = method.isAnnotationPresent(Primary.class);
            boolean isLazy = method.isAnnotationPresent(Lazy.class);

            // Override existing definition if same name
            if (container.containsBeanDefinition(primaryBeanName)) {
                container.removeBeanDefinition(primaryBeanName);
                log.info("Override bean: {}", primaryBeanName);
            }

            BeanDefinition def = BeanDefinition.of(primaryBeanName, beanType)
                    .withFactoryMethod(configBeanName, method.getName())
                    .withPrimary(isPrimary)
                    .withLazy(isLazy);
            container.registerBeanDefinition(def);
            log.info("Register @Bean: {} (factory={}#{})", def, configBeanName, method.getName());

            // Register aliases
            for (int i = 1; i < beanNames.length; i++) {
                container.registerAlias(primaryBeanName, beanNames[i]);
                log.info("Register alias: {} -> {}", beanNames[i], primaryBeanName);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void processLegacyCapperMethods(BeanContainer container, BootstrapContext context) throws Exception {
        try (ScanResult scan = new ClassGraph().enableAllInfo()
                .acceptPackages(context.getConfigurationClassParser().getComponentScanPaths())
                .scan()) {

            for (ClassInfo classInfo : scan.getAllClasses()) {
                for (MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()
                        .filter(mi -> mi.hasAnnotation(Capper.class))) {

                    // Skip if the method is in a @Configuration class (already handled via @Bean)
                    Class<?> factoryClazz = classInfo.loadClass();
                    if (factoryClazz.isAnnotationPresent(Configuration.class)) {
                        continue;
                    }

                    // Non-static factory method requires an instantiable class
                    if (!methodInfo.isStatic() && (classInfo.isInterface() || classInfo.isAbstract())) {
                        throw new BeanDefinitionException(
                                "Non-static @Capper method requires its class to be instantiable: "
                                + classInfo.getName());
                    }

                    // Ensure the factory class itself is registered
                    String factoryClassBeanName = Introspector.decapitalize(factoryClazz.getSimpleName());
                    if (!container.containsBeanDefinition(factoryClassBeanName)) {
                        BeanDefinition factoryDef = BeanDefinition.of(factoryClassBeanName, factoryClazz);
                        container.registerBeanDefinition(factoryDef);
                        log.info("Auto-register factory class bean (legacy): {}", factoryClassBeanName);
                    }

                    Method factoryMethod = methodInfo.loadClassAndGetMethod();
                    Capper capper = factoryMethod.getAnnotation(Capper.class);
                    String[] beanNames = resolveCapperMethodNames(factoryMethod, capper);
                    String primaryBeanName = beanNames[0];

                    Class<?> beanType = factoryMethod.getReturnType();

                    if (container.containsBeanDefinition(primaryBeanName)) {
                        container.removeBeanDefinition(primaryBeanName);
                        log.info("Override bean (legacy @Capper method): {}", primaryBeanName);
                    }

                    BeanDefinition def = BeanDefinition.of(primaryBeanName, beanType)
                            .withFactoryMethod(factoryClassBeanName, factoryMethod.getName())
                            .withPrimary(capper.primary())
                            .withLazy(capper.lazy())
                            .withScope(capper.scope())
                            .withDescription(capper.description());
                    container.registerBeanDefinition(def);
                    log.info("Register bean (legacy @Capper method): {}", def);

                    for (int i = 1; i < beanNames.length; i++) {
                        container.registerAlias(primaryBeanName, beanNames[i]);
                        log.info("Register alias: {} -> {}", beanNames[i], primaryBeanName);
                    }
                }
            }
        }
    }

    /**
     * 解析 {@link Bean} 方法的 Bean 名称列表。
     *
     * <p>解析规则：
     * <ol>
     *   <li>合并 {@link Bean#value()} 和 {@link Bean#name()} 中的所有名称（去重）</li>
     *   <li>若未指定，使用方法名（首字母小写）</li>
     * </ol>
     */
    private static String[] resolveBeanMethodNames(Method method, Bean beanAnnotation) {
        String defaultName = Introspector.decapitalize(method.getName());

        Set<String> resultSet = new LinkedHashSet<>();
        for (String n : beanAnnotation.value()) {
            if (n != null && !n.isBlank()) {
                resultSet.add(n);
            }
        }
        for (String n : beanAnnotation.name()) {
            if (n != null && !n.isBlank()) {
                resultSet.add(n);
            }
        }

        return resultSet.isEmpty() ? new String[]{defaultName} : resultSet.toArray(new String[0]);
    }

    @SuppressWarnings("deprecation")
    private static String[] resolveCapperMethodNames(Method method, Capper capper) {
        String defaultName = Introspector.decapitalize(method.getName());
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
