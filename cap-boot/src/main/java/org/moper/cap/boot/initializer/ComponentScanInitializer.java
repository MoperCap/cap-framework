package org.moper.cap.boot.initializer;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.moper.cap.annotation.*;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.InstantiationPolicy;
import org.moper.cap.bootstrap.Initializer;
import org.moper.cap.bootstrap.InitializerType;
import org.moper.cap.config.ConfigClassResourceViewContext;
import org.moper.cap.config.impl.DefaultConfigClassResourceViewContext;
import org.moper.cap.context.BootstrapContext;
import org.moper.cap.exception.ContextException;
import org.moper.cap.exception.InitializerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Collection;

/**
 * 组件扫描构造机，负责扫描 @Component 标注的类并注册 BeanDefinition
 */
public class ComponentScanInitializer extends Initializer {

    private static final Logger log = LoggerFactory.getLogger(ComponentScanInitializer.class);

    public ComponentScanInitializer() {
        super(InitializerType.KERNEL, 10, "ComponentScanInitializer",
                "Scans for @Component annotated classes and registers BeanDefinitions");
    }

    @Override
    public void initialize(BootstrapContext context) throws ContextException {
        String primarySourceName = context.getEnvironment().getProperty("cap.primary-source");
        if (primarySourceName == null) {
            log.warn("cap.primary-source not set, skipping component scan");
            return;
        }

        Class<?> primarySource;
        try {
            primarySource = Class.forName(primarySourceName);
        } catch (ClassNotFoundException e) {
            throw new InitializerException("Cannot load primary source class: " + primarySourceName, e);
        }

        ConfigClassResourceViewContext viewContext = new DefaultConfigClassResourceViewContext(primarySource);
        Collection<String> scanPackages = viewContext.getComponentScanPaths();

        try (ScanResult scanResult = new ClassGraph()
                .enableAnnotationInfo()
                .enableClassInfo()
                .acceptPackages(scanPackages.toArray(new String[0]))
                .scan()) {

            ClassInfoList componentClasses = scanResult.getClassesWithAnnotation(
                    Component.class.getName());

            for (ClassInfo classInfo : componentClasses) {
                if (classInfo.isAbstract() || classInfo.isInterface() || classInfo.isAnnotation()) {
                    continue;
                }
                try {
                    Class<?> beanClass = classInfo.loadClass();
                    String beanName = toBeanName(beanClass.getSimpleName());
                    BeanDefinition definition = buildBeanDefinition(beanName, beanClass);
                    context.getBeanContainer().registerBeanDefinition(definition);
                    log.debug("Registered bean: {} -> {}", beanName, beanClass.getName());
                } catch (Exception e) {
                    log.warn("Failed to register bean for class {}: {}", classInfo.getName(), e.getMessage());
                }
            }
        }
    }

    private BeanDefinition buildBeanDefinition(String beanName, Class<?> beanClass) {
        BeanDefinition definition = BeanDefinition.of(beanName, beanClass);

        // Handle @Lazy
        if (beanClass.isAnnotationPresent(Lazy.class)) {
            definition = definition.withLazy(true);
        }

        // Handle @Primary
        if (beanClass.isAnnotationPresent(Primary.class)) {
            definition = definition.withPrimary(true);
        }

        // Handle instantiation policy based on constructors
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        if (constructors.length == 1 && constructors[0].getParameterCount() > 0) {
            // Only one constructor with parameters - use it
            Class<?>[] paramTypes = constructors[0].getParameterTypes();
            definition = definition.withInstantiationPolicy(InstantiationPolicy.constructor(paramTypes));
        } else {
            // Check if there is exactly one @Autowired constructor
            Constructor<?> autowiredConstructor = null;
            boolean hasNoArg = false;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 0) {
                    hasNoArg = true;
                }
                if (c.isAnnotationPresent(Autowired.class)) {
                    autowiredConstructor = c;
                }
            }
            if (autowiredConstructor != null && !hasNoArg) {
                Class<?>[] paramTypes = autowiredConstructor.getParameterTypes();
                definition = definition.withInstantiationPolicy(InstantiationPolicy.constructor(paramTypes));
            }
            // Otherwise use default no-arg constructor (default in BeanDefinition.of)
        }

        return definition;
    }

    private String toBeanName(String className) {
        if (className == null || className.isEmpty()) return className;
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}
