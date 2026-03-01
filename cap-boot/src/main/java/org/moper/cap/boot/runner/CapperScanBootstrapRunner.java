package org.moper.cap.boot.runner;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.instantiation.ConstructorInstantiation;
import org.moper.cap.bean.definition.instantiation.FactoryInstantiation;
import org.moper.cap.bean.definition.instantiation.InstantiationPolicy;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@RunnerMeta(type = RunnerType.KERNEL, order = 300, description = "Scan all @Capper Classes and Methods to register Bean Definitions")
public class CapperScanBootstrapRunner implements BootstrapRunner {

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
        Collection<String> scanPackages = context.getConfigurationClassParser().getComponentScanPaths();
        // 扫描所有 @Capper 注解标记的类和方法，并注册对应的 Bean 定义
        try(ScanResult scan = new ClassGraph().enableAllInfo().acceptPackages(scanPackages.toArray(new String[0])).scan()) {

            // 收集@Capper类并注册Bean定义
            for(ClassInfo classInfo : scan.getClassesWithAnnotation(Capper.class.getName())
                    .filter(classInfo -> !classInfo.isInterface() && !classInfo.isAbstract() && !classInfo.isAnnotation())) {

                Class<?> clazz = classInfo.loadClass();
                // Bean 名称（支持多个别名，第一个名称会被作为主要名称）
                String[] beanNames = resolveBeanNames(clazz);
                String primaryBeanName = beanNames[0];
                Capper capper = clazz.getAnnotation(Capper.class);
                // 注册Bean定义
                BeanDefinition def = BeanDefinition.of(primaryBeanName, clazz)
                        .withPrimary(capper.primary())
                        .withLazy(capper.lazy())
                        .withScope(capper.scope())
                        .withDescription(capper.description());
                beanDefinitionMap.put(primaryBeanName, def);
                // 注册别名
                if(beanNames.length > 1) {
                    Set<String> aliasSet = new HashSet<>(Arrays.asList(beanNames).subList(1, beanNames.length));
                    aliasMap.put(primaryBeanName, aliasSet);
                }
            }

            // 收集@Capper方法并注册Bean定义
            for(ClassInfo classInfo : scan.getAllClasses()){
                for(MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()
                        .filter(methodInfo -> methodInfo.hasAnnotation(Capper.class.getName()))) {

                    Class<?> factoryClazz = classInfo.loadClass();
                    String factoryClassBeanName = resolveBeanNames(factoryClazz)[0];

                    Method factoryMethod = methodInfo.loadClassAndGetMethod();
                    String factoryMethodName = factoryMethod.getName();
                    Class<?>[] factoryMethodArgTypes = factoryMethod.getParameterTypes();

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

                    FactoryInstantiation policy = FactoryInstantiation.of(factoryClassBeanName, factoryMethodName, factoryMethodArgTypes);

                    // Bean 名称（支持多个别名，第一个名称会被作为主要名称）
                    String[] beanNames = resolveBeanNames(factoryMethod);
                    String primaryBeanName = beanNames[0];
                    Class<?> beanType = factoryMethod.getReturnType();
                    Capper capper = factoryMethod.getAnnotation(Capper.class);

                    // 注册Bean定义
                    BeanDefinition def = BeanDefinition.of(primaryBeanName, beanType)
                            .withPrimary(capper.primary())
                            .withLazy(capper.lazy())
                            .withScope(capper.scope())
                            .withDescription(capper.description())
                            .withInstantiationPolicy(policy);
                    beanDefinitionMap.put(primaryBeanName, def);
                    // 注册别名
                    if(beanNames.length > 1) {
                        Set<String> aliasSet = new HashSet<>(Arrays.asList(beanNames).subList(1, beanNames.length));
                        aliasMap.put(primaryBeanName, aliasSet);
                    }

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

    /**
     * 解析Bean名称，优先使用 @Capper 注解中指定的名称，如果未指定则使用默认名称（类名或方法名首字母小写）
     *
     * @param element 类或方法元素
     * @return Bean名称数组，第一个元素为主要名称，后续元素为别名
     */
    private String[] resolveBeanNames(AnnotatedElement element) {
        String defaultName = null;
        switch (element){
            case Class<?> clazz -> defaultName = decapitalize(clazz.getSimpleName());
            case Method method -> defaultName = decapitalize(method.getName());
            default -> throw new IllegalStateException("Unexpected value: " + element);
        }
        Capper capper = element.getAnnotation(Capper.class);
        if(capper == null) {
            return new String[]{defaultName};
        }
        String[] beanNames = capper.names();
        if(beanNames == null || beanNames.length == 0) {
            return new String[]{defaultName};
        }
        return beanNames;
    }

    /**
     * 将类名或方法名首字母小写作为默认 Bean 名称
     *
     * @param name 类名或方法名
     * @return 首字母小写的名称
     */
    private String decapitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        if (Character.isLowerCase(name.charAt(0))) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
