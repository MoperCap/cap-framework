package org.moper.cap.boot.runner;

import io.github.classgraph.*;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.bean.util.BeanNamesResolver;
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

    @Override
    public void initialize(BootstrapContext context) throws Exception {
        BeanContainer container = context.getBeanContainer();

        try (ScanResult scan = new ClassGraph().enableAllInfo()
                .acceptPackages(context.getConfigurationClassParser().getComponentScanPaths())
                .scan()) {

            for (ClassInfo classInfo : scan.getAllClasses()) {
                for (MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()
                        .filter(mi -> mi.hasAnnotation(Capper.class))) {

                    // 非静态工厂方法所在类必须是可实例化的
                    if (!methodInfo.isStatic() && (classInfo.isInterface() || classInfo.isAbstract())) {
                        throw new BeanDefinitionException(
                                "Non-static @Capper method requires its class be instantiable: " + classInfo.getName());
                    }

                    Class<?> factoryClazz = classInfo.loadClass();
                    String factoryClassBeanName = BeanNamesResolver.resolve(factoryClazz)[0];

                    // 工厂方法所在的类无论是否有 @Capper 注解，都需要被管理
                    // 由于在 ClassBeanRegisterBootstrapRunner 中已经处理了所有的 @Capper 类
                    // 因此此处的类只要不在 BeanContainer 的注册中，就一定是非 @Capper 类
                    if(!container.containsBeanDefinition(factoryClassBeanName)) {
                        BeanDefinition factoryDef = BeanDefinition.of(factoryClassBeanName, factoryClazz);
                        container.registerBeanDefinition(factoryDef);
                    }

                    Method factoryMethod = methodInfo.loadClassAndGetMethod();
                    String factoryMethodName = factoryMethod.getName();
                    String[] factoryMethodParameterBeanNames = resolveMethodParameterBeanNames(factoryMethod);

                    String[] beanNames = BeanNamesResolver.resolve(factoryMethod);
                    String primaryBeanName = beanNames[0];

                    Class<?> beanType = factoryMethod.getReturnType();
                    Capper capper = factoryMethod.getAnnotation(Capper.class);

                    // 注册Bean定义
                    BeanDefinition def = BeanDefinition.of(primaryBeanName, beanType)
                            .withFactoryMethod(factoryClassBeanName, factoryMethodName)
                            .withParameterBeanNames(factoryMethodParameterBeanNames)
                            .withPrimary(capper.primary())
                            .withLazy(capper.lazy())
                            .withScope(capper.scope())
                            .withDescription(capper.description());
                    // 若已经存在Bean定义，则进行覆盖
                    // 目前已知的满足条件有：当@Capper方法的返回类型上存在@Capper注解，且两者均未显式定义Bean名称，则@Capper方法的Bean定义会覆盖@Capper类的Bean定义
                    if(container.containsBeanDefinition(primaryBeanName)) {
                        container.removeBeanDefinition(primaryBeanName);
                        log.info("Override bean: {}", primaryBeanName);
                    }
                    container.registerBeanDefinition(def);
                    log.info("Register bean: {}", def);

                    // 注册别名
                    if(beanNames.length > 1) {
                        for(int i = 1; i < beanNames.length; i++) {
                            String alias = beanNames[i];
                            container.registerAlias(primaryBeanName, alias);
                            log.info("Register alias: {} -> {}", alias, primaryBeanName);
                        }
                    }
                }
            }
        }
    }

    private String[] resolveMethodParameterBeanNames(Method method) {
        Parameter[] parameters = method.getParameters();
        String[] beanNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            beanNames[i] = BeanNamesResolver.resolve(parameters[i]);
        }
        return beanNames;
    }
}
