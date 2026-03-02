package org.moper.cap.boot.runner;

import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.bean.util.BeanNamesResolver;
import org.moper.cap.boot.resolver.LifecycleMethodResolver;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 扫描 @Capper 标注的类并注册 BeanDefinition（构造函数实例化）。
 *
 * <p>执行顺序为 300，在 {@link FactoryBeanRegisterBootstrapRunner}（310）之前执行。
 */
@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 300, description = "Scan @Capper annotated classes and register Bean Definitions (constructor instantiation)")
public class ClassBeanRegisterBootstrapRunner implements BootstrapRunner {

    private final Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
    private final Map<String, Set<String>> aliasMap = new LinkedHashMap<>();

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        try (ScanResult scan = new ClassGraph().enableAllInfo()
                .acceptPackages(context.getConfigurationClassParser().getComponentScanPaths())
                .scan()) {

            for (ClassInfo classInfo : scan.getClassesWithAnnotation(Capper.class)
                    .filter(ci -> !ci.isInterface() && !ci.isAbstract() && !ci.isAnnotation())) {

                Class<?> clazz = classInfo.loadClass();

                String[] constructorParameterBeanNames = resolveConstructorParameterBeanNames(classInfo);
                String[] beanNames = BeanNamesResolver.resolveClass(clazz);
                String primaryBeanName = beanNames[0];
                Capper capper = clazz.getAnnotation(Capper.class);

                // 验证生命周期方法
                LifecycleMethodResolver.validate(clazz, capper.initMethod());
                LifecycleMethodResolver.validate(clazz, capper.destroyMethod());

                BeanDefinition def = BeanDefinition.of(primaryBeanName, clazz)
                        .withParameterBeanNames(constructorParameterBeanNames)
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

        BeanContainer container = context.getBeanContainer();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String primaryName = entry.getKey();
            BeanDefinition def = entry.getValue();
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

    private String[] resolveConstructorParameterBeanNames(ClassInfo info) {
        MethodInfoList constructors = info.getDeclaredConstructorInfo();
        MethodInfoList injectConstructors = constructors.filter(mi -> mi.hasAnnotation(Inject.class));
        Constructor<?> constructor;
        if (injectConstructors.size() > 1) {
            throw new BeanDefinitionException("Multiple constructors annotated with @Inject found in class: " + info.getName());
        } else if (injectConstructors.size() == 1) {
            constructor = injectConstructors.getFirst().loadClassAndGetConstructor();
        } else if (constructors.size() > 1) {
            throw new BeanDefinitionException("Multiple constructors found in class without @Inject annotation: " + info.getName());
        } else if (constructors.size() == 1) {
            constructor = constructors.getFirst().loadClassAndGetConstructor();
        } else {
            return new String[0];
        }
        Parameter[] parameters = constructor.getParameters();
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
