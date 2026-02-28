package org.moper.cap.boot.runner;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.BeanScope;
import org.moper.cap.bean.definition.InstantiationPolicy;
import org.moper.cap.bean.exception.BeanDefinitionException;
import org.moper.cap.core.annotation.RunnerMeta;
import org.moper.cap.core.context.BootstrapContext;
import org.moper.cap.core.runner.BootstrapRunner;
import org.moper.cap.core.runner.RunnerType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@RunnerMeta(type = RunnerType.KERNEL, order = 100, description = "")
public class CapperScanBootstrapRunner implements BootstrapRunner {

    /**
     * 缓存所有被 @Capper 注解标记的类
     */
    private final Set<Class<?>> capperTypeSet = new HashSet<>();

    /**
     * 缓存所有被 @Capper 注解标记的方法
     */
    private final Set<Method> capperMethodSet = new HashSet<>();

    /**
     * 缓存所有未被 @Capper 注解标记但被工厂Bean方法所依赖的可实例化类
     */
    private final Set<Class<?>> extraTypeSet = new HashSet<>();

    /**
     * 缓存所有 Bean 定义，key 为 Bean 名称，value 为 Bean 定义信息
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();

    /**
     * 缓存 Bean 别名映射，key 为主要 Bean 名称，value 为别名列表
     */
    private Map<String, Set<String>> aliasMap = new LinkedHashMap<>();
    /**
     * 框架初始化阶段执行器 </br>
     *
     * @param context 框架初始化阶段系统上下文
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public void initialize(BootstrapContext context) throws Exception {
        Collection<String> scanPackages = context.getConfigurationClassParser().getComponentScanPaths();
        try(ScanResult scan = new ClassGraph().enableAllInfo().acceptPackages(scanPackages.toArray(new String[0])).scan()) {

            // 收集@Capper类
            for(ClassInfo classInfo : scan.getClassesWithAnnotation(Capper.class.getName())
                    .filter(classInfo -> !classInfo.isInterface() && !classInfo.isAbstract() && !classInfo.isAnnotation())) {
                capperTypeSet.add(classInfo.loadClass());
            }

            // 收集@Capper方法并检测依赖（工厂类）
            for(ClassInfo classInfo : scan.getAllClasses()){
                for(MethodInfo methodInfo : classInfo.getDeclaredMethodInfo()
                        .filter(methodInfo -> methodInfo.hasAnnotation(Capper.class.getName()))) {

                    Method method = methodInfo.loadClassAndGetMethod();
                    capperMethodSet.add(method);

                    // 若方法为static方法，则无需依赖工厂类实例
                    if(methodInfo.isStatic()) continue;
                    // 若方法所在类被 @Capper 注解标记，则无需额外依赖
                    if(classInfo.hasAnnotation(Capper.class)) continue;
                    // 若方法所在类不可实例化，则无法作为工厂类，抛出异常
                    if(classInfo.isInterface() || classInfo.isAbstract()) {
                        throw new BeanDefinitionException("Non-static @Capper method requires its class be instantiable: " + classInfo.getName())
                    }
                    // 将工厂类加入额外类型集合，以便后续扫描类级 @Capper 时进行依赖检查
                    extraTypeSet.add(classInfo.loadClass());

                }
            }

            // 采用默认方式处理 extraTypeSet
            for(Class<?> extraType : extraTypeSet) {
                String beanName = decapitalize(extraType.getSimpleName());
                BeanDefinition def = BeanDefinition.of(beanName, extraType);
                beanDefinitionMap.put(beanName, def);
            }
            // 处理 capperTypeSet
            for(Class<?> capperType : capperTypeSet) {
                Capper capper = capperType.getAnnotation(Capper.class);
                // Bean 名称（支持多个别名，第一个名称会被作为主要名称）
                String[] beanNames = capper.names();
                if(beanNames == null || beanNames.length == 0) {
                    beanNames = new String[]{decapitalize(capperType.getSimpleName())};
                } else if (beanNames.length > 1) {
                    // 注册别名
                    Set<String> aliasSet = new HashSet<>(Arrays.asList(beanNames).subList(1, beanNames.length));
                    aliasMap.put(beanNames[0], aliasSet);
                }

                // 注册Bean定义
                BeanDefinition def = BeanDefinition.of(beanNames[0], capperType)
                        .withPrimary(capper.primary())
                        .withLazy(capper.lazy())
                        .withScope(capper.scope())
                        .withDescription(capper.description());
                beanDefinitionMap.put(beanNames[0], def);
            }
            // 处理 capperMethodSet
            for(Method method : capperMethodSet) {
                Capper capper = method.getAnnotation(Capper.class);
                // Bean 名称（支持多个别名，第一个名称会被作为主要名称）
                String[] beanNames = capper.names();
                // 若未指定 Bean 名称，则默认使用返回类型的驼峰命名作为 Bean 名称
                if(beanNames == null || beanNames.length == 0) {
                    beanNames = new String[]{decapitalize(method.getReturnType().getSimpleName())};
                } else if (beanNames.length > 1) {
                    Set<String> aliasSet = new HashSet<>(Arrays.asList(beanNames).subList(1, beanNames.length));
                    aliasMap.put(beanNames[0], aliasSet);
                }

                // TODO: 根据@Capper方法指定Bean实例化方式
                // TODO: 解决@Capper方法与@Capper指定同一类型的冲突问题
            }

        }
    }

    private BeanDefinition createBeanDefinitionByType(Class<?> clazz, Map<String, String> alias) {
        Capper capper = clazz.getAnnotation(Capper.class);
        // Bean 名称（支持多个别名，第一个名称会被作为主要名称）
        String[] beanNames = capper.names();
        if(beanNames == null || beanNames.length == 0) {
            beanNames = new String[]{decapitalize(clazz.getSimpleName())};
        } else if (beanNames.length > 1) {
            // 注册别名
            for(int i = 1; i < beanNames.length; i++) {
                alias.put(beanNames[0], beanNames[i]);
            }
        }

        // Bean 是否为首选
        boolean primary = capper.primary();
        // Bean 是否为懒加载
        boolean lazy = capper.lazy();
        // Bean 作用域
        BeanScope scope = capper.scope();
        // Bean 描述信息
        String description = capper.description();
        // Bean实例化方式为构造函数，参数类型为无参构造
        InstantiationPolicy policy = InstantiationPolicy.constructor();

        return BeanDefinition.of(beanNames[0], clazz)
                .withPrimary(primary)
                .withLazy(lazy)
                .withScope(scope)
                .withDescription(description)
                .withInstantiationPolicy(policy);
    }

    private BeanDefinition createBeanDefinitionByMethod(Method method, Map<String, String> alias) {
        Capper capper = method.getAnnotation(Capper.class);
        // Bean 名称（支持多个别名，第一个名称会被作为主要名称）
        String[] beanNames = capper.names();
        if(beanNames == null || beanNames.length == 0) {
            beanNames = new String[]{decapitalize(method.getReturnType().getSimpleName())};
        } else if (beanNames.length > 1) {
            // 注册别名
            for(int i = 1; i < beanNames.length; i++) {
                alias.put(beanNames[0], beanNames[i]);
            }
        }

        // Bean 是否为首选
        boolean primary = capper.primary();
        // Bean 是否为懒加载
        boolean lazy = capper.lazy();
        // Bean 作用域
        BeanScope scope = capper.scope();
        // Bean 描述信息
        String description = capper.description();
        // Bean实例化方式为工厂方法，返回类型为Bean类型
        InstantiationPolicy policy = Modifier.isStatic(method.getModifiers()) ?
                InstantiationPolicy.staticFactory(method.getName(), method.getParameterTypes()) :
                InstantiationPolicy.instanceFactory(method.getDeclaringClass().getSimpleName(), method.getName(), method.getParameterTypes());

        return BeanDefinition.of(beanNames[0], method.getReturnType())
                .withPrimary(primary)
                .withLazy(lazy)
                .withScope(scope)
                .withDescription(description)
                .withInstantiationPolicy(policy);
    }

    private String decapitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        if (Character.isLowerCase(name.charAt(0))) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
