package org.moper.cap.boot.runner;

import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.bean.util.BeanNamesResolver;
import org.moper.cap.boot.resolver.LifecycleMethodResolver;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 扫描 @Capper 标注的方法并注册 BeanDefinition（工厂方法实例化）。
 *
 * <p>执行顺序为 310，在 {@link ClassBeanRegisterBootstrapRunner}（300）之后执行。
 * 工厂 Bean 可以覆盖同名的普通 Bean（由类注册的 Bean）。
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 310, description = "Scan @Capper annotated methods and register Bean Definitions (factory method instantiation)")
public class FactoryBeanRegisterBootstrapRunner implements BootstrapRunner {

    private final Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
    private final Map<String, Set<String>> aliasMap = new LinkedHashMap<>();

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        try (ScanResult scan = new ClassGraph().enableAllInfo()
                .acceptPackages(context.getConfigurationClassParser().getComponentScanPaths())
                .scan()) {

            for (ClassInfo classInfo : scan.getAllClasses()) {
                for (MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()
                        .filter(mi -> mi.hasAnnotation(Capper.class))) {

                    Class<?> factoryClazz = classInfo.loadClass();
                    String factoryClassBeanName = BeanNamesResolver.resolveClass(factoryClazz)[0];

                    Method factoryMethod = methodInfo.loadClassAndGetMethod();
                    String factoryMethodName = factoryMethod.getName();

                    // 非静态工厂方法所在类必须是可实例化的
                    if (!methodInfo.isStatic() && (classInfo.isInterface() || classInfo.isAbstract())) {
                        throw new BeanDefinitionException(
                                "Non-static @Capper method requires its class be instantiable: " + classInfo.getName());
                    }

                    // 工厂类未注册时自动注册（无论是否带 @Capper 注解）
                    if (!beanDefinitionMap.containsKey(factoryClassBeanName)
                            && !context.getBeanContainer().containsBeanDefinition(factoryClassBeanName)) {
                        BeanDefinition factoryDef = BeanDefinition.of(factoryClassBeanName, factoryClazz);
                        beanDefinitionMap.put(factoryClassBeanName, factoryDef);
                    }

                    String[] factoryMethodParameterBeanNames = resolveMethodParameterBeanNames(factoryMethod);
                    String[] beanNames = BeanNamesResolver.resolveMethod(factoryMethod);
                    String primaryBeanName = beanNames[0];
                    Class<?> beanType = factoryMethod.getReturnType();
                    Capper capper = factoryMethod.getAnnotation(Capper.class);

                    // 验证生命周期方法
                    LifecycleMethodResolver.validate(beanType, capper.initMethod());
                    LifecycleMethodResolver.validate(beanType, capper.destroyMethod());

                    BeanDefinition def = BeanDefinition.of(primaryBeanName, beanType)
                            .withFactoryMethod(factoryClassBeanName, factoryMethodName)
                            .withParameterBeanNames(factoryMethodParameterBeanNames)
                            .withPrimary(capper.primary())
                            .withLazy(capper.lazy())
                            .withScope(capper.scope())
                            .withDescription(capper.description())
                            .withInitMethod(capper.initMethod().isBlank() ? null : capper.initMethod())
                            .withDestroyMethod(capper.destroyMethod().isBlank() ? null : capper.destroyMethod());
                    beanDefinitionMap.put(primaryBeanName, def);
                    registerAlias(beanNames);
                }
            }
        }

        BeanContainer container = context.getBeanContainer();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String primaryName = entry.getKey();
            BeanDefinition def = entry.getValue();

            // 工厂 Bean 可以覆盖同名的普通 Bean
            if (container.containsBeanDefinition(primaryName)) {
                container.removeBeanDefinition(primaryName);
                log.info("Override bean: {}", primaryName);
            }
            container.registerBeanDefinition(def);
            log.info("Register bean: {}", def);
            if (aliasMap.containsKey(primaryName)) {
                for (String alias : aliasMap.get(primaryName)) {
                    container.registerAlias(primaryName, alias);
                    log.info("Register alias: {} -> {}", alias, primaryName);
                }
            }
        }
    }

    private String[] resolveMethodParameterBeanNames(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] beanNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            beanNames[i] = BeanNamesResolver.resolveParameter(parameters[i]);
        }
        return beanNames;
    }

    private void registerAlias(String[] beanNames) {
        String primaryBeanName = beanNames[0];
        if (beanNames.length > 1) {
            Set<String> aliasSet = new HashSet<>(Arrays.asList(beanNames).subList(1, beanNames.length));
            aliasMap.put(primaryBeanName, aliasSet);
        }
    }
}
