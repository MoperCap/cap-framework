package org.moper.cap.boot.runner;

import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.InstantiationPolicy;
import org.moper.cap.bean.annotation.Autowired;
import org.moper.cap.bean.annotation.Lazy;
import org.moper.cap.bean.annotation.Primary;
import org.moper.cap.bean.annotation.Bean;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.annotation.Component;
import org.moper.cap.core.annotation.Configuration;
import org.moper.cap.core.runner.RunnerType;
import org.moper.cap.core.context.BootstrapContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 组件扫描构造机，负责扫描 @Component 标注的类并注册 BeanDefinition
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 10, name = "CapComponentScanBootstrapRunner", description = "Scans for @Component annotated classes and registers BeanDefinitions")
public class ComponentScanBootstrapRunner implements BootstrapRunner {

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        Collection<String> scanPackages = context.getConfigurationClass().getComponentScanPaths();

        try (ScanResult scanResult = new ClassGraph()
                .enableAnnotationInfo()
                .enableClassInfo()
                .enableMethodInfo()
                .acceptPackages(scanPackages.toArray(new String[0]))
                .scan()) {

            // 扫描所有 @Component 注解的类，并注册为 BeanDefinition
            for (ClassInfo classInfo :
                    scanResult.getClassesWithAnnotation(Component.class.getName())
                    .filter(classInfo -> !classInfo.isAbstract() && !classInfo.isInterface() && !classInfo.isAnnotation()))
            {
                try {
                    // 注册所有 @Component 注解的类为 BeanDefinition
                    Class<?> beanClass = classInfo.loadClass();
                    String beanName = toBeanName(beanClass.getSimpleName());
                    BeanDefinition definition = buildBeanDefinition(beanName, beanClass);
                    context.getBeanContainer().registerBeanDefinition(definition);
                    log.debug("Registered bean: {} -> {}", beanName, beanClass.getName());

                    // 如果是 @Configuration 类，还需要注册它的 @Bean 方法
                    if(!classInfo.hasAnnotation(Configuration.class)) continue;

                    for(MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()
                            .filter(method -> method.hasAnnotation(Bean.class)))
                    {
                        Method method = methodInfo.loadClassAndGetMethod();
                        String methodName = method.getName();
                        Bean methodBean = method.getAnnotation(Bean.class);
                        String methodBeanName = methodBean.value().isEmpty() ? toBeanName(methodName) : methodBean.value();
                        Class<?> methodBeanType = method.getReturnType();
                        Class<?>[] methodBeanArgTypes = method.getParameterTypes();

                        InstantiationPolicy methodInstantiationPolicy = methodInfo.isStatic() ?
                                InstantiationPolicy.staticFactory(methodName, methodBeanArgTypes) :
                                InstantiationPolicy.instanceFactory(beanName, methodName, methodBeanArgTypes);

                        BeanDefinition methodBeanDefinition = BeanDefinition.of(methodBeanName, methodBeanType)
                                .withInstantiationPolicy(methodInstantiationPolicy);

                        context.getBeanContainer().registerBeanDefinition(methodBeanDefinition);

                        log.debug("Registering @Bean: {} -> {} ", methodBeanName, methodBeanType);
                    }

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
            for (Constructor<?> c : constructors) {
                if (c.isAnnotationPresent(Autowired.class)) {
                    autowiredConstructor = c;
                }
            }
            if (autowiredConstructor != null) {
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