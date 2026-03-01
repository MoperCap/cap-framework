package org.moper.cap.boot.runner;

import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.boot.util.BeanNamesResolver;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 300, description = "Scan all @Capper Classes and Methods to register Bean Definitions")
public class BeanDefinitionRegisterBootstrapRunner implements BootstrapRunner {

    /**
     * 缓存所有 Bean 定义，key 为 Bean 名称，value 为 Bean 定义信息
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();

    /**
     * 缓存 Bean 别名映射，key 为主要 Bean 名称，value 为别名列表
     */
    private final Map<String, Set<String>> aliasMap = new LinkedHashMap<>();
    /**
     * 框架初始化阶段执行器 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void initialize(BootstrapContext context) throws Exception {
        // 扫描所有 @Capper 注解标记的类和方法，并注册对应的 Bean 定义
        try(ScanResult scan = new ClassGraph().enableAllInfo()
                .acceptPackages(context.getConfigurationClassParser().getComponentScanPaths())
                .scan()) {

            // 收集@Capper类并注册Bean定义
            for(ClassInfo classInfo : scan.getClassesWithAnnotation(Capper.class)
                    .filter(classInfo -> !classInfo.isInterface() && !classInfo.isAbstract() && !classInfo.isAnnotation())) {

                Class<?> clazz = classInfo.loadClass();

                // Bean 构造函数参数 Bean 名称
                String[] constructorArgBeanNames = resolveConstructorArgBeanNames(classInfo);
                // Bean 名称（支持多个别名，第一个名称会被作为主要名称）
                String[] beanNames = BeanNamesResolver.resolve(clazz);
                String primaryBeanName = beanNames[0];
                Capper capper = clazz.getAnnotation(Capper.class);

                // 注册Bean定义
                BeanDefinition def = BeanDefinition.of(primaryBeanName, clazz)
                        .withConstructorArgs(constructorArgBeanNames)
                        .withPrimary(capper.primary())
                        .withLazy(capper.lazy())
                        .withScope(capper.scope())
                        .withDescription(capper.description());
                beanDefinitionMap.put(primaryBeanName, def);
                // 注册别名
                registerAlias(beanNames);
            }

            // 收集@Capper方法并注册Bean定义
            for(ClassInfo classInfo : scan.getAllClasses()){
                for(MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()
                        .filter(methodInfo -> methodInfo.hasAnnotation(Capper.class))) {

                    Class<?> factoryClazz = classInfo.loadClass();
                    String factoryClassBeanName = BeanNamesResolver.resolve(factoryClazz)[0];

                    Method factoryMethod = methodInfo.loadClassAndGetMethod();
                    String factoryMethodName = factoryMethod.getName();

                    // 非静态工厂方法所在类必须是可实例化的，否则无法作为工厂类，抛出异常
                    if(!methodInfo.isStatic() && (classInfo.isInterface() || classInfo.isAbstract())) {
                        throw new BeanDefinitionException("Non-static @Capper method requires its class be instantiable: " + classInfo.getName());
                    }

                    // 无论工厂方法所在类是否被 @Capper 注解标记，只要该类未被注册为 Bean，就先将该类注册为 Bean
                    // 由于已经检索了所有的 @Capper 注解标记的类并注册了 Bean 定义
                    // 因此只有当工厂方法所在的类未被 @Capper 注解标记且未被注册为 Bean 时才会执行注册工厂类的 Bean 定义的逻辑
                    if(!beanDefinitionMap.containsKey(factoryClassBeanName)) {
                        BeanDefinition factoryDef = BeanDefinition.of(factoryClassBeanName, factoryClazz);
                        beanDefinitionMap.put(factoryClassBeanName, factoryDef);
                    }

                    // 解析工厂方法参数 Bean 名称
                    String[] factoryMethodArgBeanNames = resolveMethodArgBeanNames(factoryMethod);

                    // Bean 名称（支持多个别名，第一个名称会被作为主要名称）
                    String[] beanNames = BeanNamesResolver.resolve(factoryMethod);
                    String primaryBeanName = beanNames[0];
                    Class<?> beanType = factoryMethod.getReturnType();
                    Capper capper = factoryMethod.getAnnotation(Capper.class);

                    // 注册Bean定义
                    BeanDefinition def = BeanDefinition.of(primaryBeanName, beanType)
                            .withFactoryMethod(factoryClassBeanName, factoryMethodName, factoryMethodArgBeanNames)
                            .withPrimary(capper.primary())
                            .withLazy(capper.lazy())
                            .withScope(capper.scope())
                            .withDescription(capper.description());
                    beanDefinitionMap.put(primaryBeanName, def);
                    // 注册别名
                    registerAlias(beanNames);

                }
            }
        }

        // 在扫描完成后将所有 Bean 定义与别名注册到上下文的 Bean 容器中
        BeanContainer container = context.getBeanContainer();
        for(Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            // 注册 Bean 定义
            String primaryName = entry.getKey();
            BeanDefinition def = entry.getValue();
            container.registerBeanDefinition(def);
            log.info("Register bean: {}", def);
            // 注册别名
            if(aliasMap.containsKey(primaryName)) {
                for(String alias : aliasMap.get(primaryName)) {
                    container.registerAlias(primaryName, alias);
                    log.info("Register alias: {} -> {}", alias, primaryName);
                }
            }
        }

    }

    private String[] resolveConstructorArgBeanNames(ClassInfo info) {
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
            // 无构造函数，使用默认无参构造函数
            return new String[0];
        }
        return resolveMethodArgBeanNames(constructor.getParameterTypes());
    }

    private String[] resolveMethodArgBeanNames(Method method) {
        return resolveMethodArgBeanNames(method.getParameterTypes());
    }

    private String[] resolveMethodArgBeanNames(Class<?>[] paramTypes) {
        String[] argBeanNames = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            argBeanNames[i] = BeanNamesResolver.resolve(paramTypes[i])[0];
        }
        return argBeanNames;
    }

    /**
     * 注册 Bean 别名，支持一个 Bean 定义多个别名，第一个名称会被作为主要名称
     *
     * @param beanNames Bean 名称列表，第一个名称会被作为主要名称，后续名称会被作为别名
     */
    private void registerAlias(String[] beanNames){
        String primaryBeanName = beanNames[0];
        if(beanNames.length > 1) {
            Set<String> aliasSet = new HashSet<>(Arrays.asList(beanNames).subList(1, beanNames.length));
            aliasMap.put(primaryBeanName, aliasSet);
        }
    }
}
