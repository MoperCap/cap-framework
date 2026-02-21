package org.moper.cap.bean.factory.impl;

import org.jetbrains.annotations.Nullable;
import org.moper.cap.bean.aware.BeanClassLoaderAware;
import org.moper.cap.bean.aware.BeanFactoryAware;
import org.moper.cap.bean.aware.BeanNameAware;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.definition.BeanScope;
import org.moper.cap.bean.exception.*;
import org.moper.cap.bean.factory.AutowireCapableBeanFactory;
import org.moper.cap.bean.factory.ConfigurableBeanFactory;
import org.moper.cap.bean.factory.ListableBeanFactory;
import org.moper.cap.bean.lifecycle.DisposableBean;
import org.moper.cap.bean.lifecycle.InitializingBean;
import org.moper.cap.bean.processor.BeanPostProcessor;
import org.moper.cap.bean.registry.BeanDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DefaultBeanFactory - Bean工厂的默认实现
 * 实现了 ConfigurableBeanFactory、ListableBeanFactory、AutowireCapableBeanFactory 和 BeanDefinitionRegistry 接口，
 * 提供完整的IoC容器功能。
 */
public class DefaultBeanFactory implements ConfigurableBeanFactory, ListableBeanFactory,
        AutowireCapableBeanFactory, BeanDefinitionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBeanFactory.class);

    /** Bean定义注册表：beanName -> BeanDefinition */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /** 单例缓存：beanName -> 实例 */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    /** 别名映射：alias -> 正规beanName */
    private final Map<String, String> aliasMap = new ConcurrentHashMap<>();

    /** Bean后置处理器列表 */
    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    /** 正在创建中的Bean（用于循环依赖检测） */
    private final Set<String> currentlyInCreation = Collections.synchronizedSet(new HashSet<>());

    /** 配置冻结标志 */
    private volatile boolean configurationFrozen = false;

    // ===================== BeanFactory =====================

    @Override
    public Object getBean(String beanName) throws BeanException {
        return doGetBean(beanName, null);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) throws BeanException {
        Object bean = doGetBean(beanName, requiredType);
        if (!requiredType.isInstance(bean)) {
            throw new BeanNotOfRequiredTypeException(beanName, requiredType, bean.getClass());
        }
        return requiredType.cast(bean);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeanException {
        String[] names = getBeanNamesForType(requiredType);
        if (names.length == 0) {
            throw new NoSuchBeanDefinitionException(requiredType);
        }
        if (names.length == 1) {
            return getBean(names[0], requiredType);
        }
        // 多个候选：优先选 primary
        String primaryName = null;
        for (String name : names) {
            BeanDefinition def = beanDefinitionMap.get(name);
            if (def != null && def.primary()) {
                if (primaryName != null) {
                    throw new NoUniqueBeanDefinitionException(requiredType, names);
                }
                primaryName = name;
            }
        }
        if (primaryName == null) {
            throw new NoUniqueBeanDefinitionException(requiredType, names);
        }
        return getBean(primaryName, requiredType);
    }

    @Override
    public boolean containsBean(String beanName) {
        String canonical = resolveAlias(beanName);
        return beanDefinitionMap.containsKey(canonical) || singletonObjects.containsKey(canonical);
    }

    @Override
    public boolean isSingleton(String beanName) throws BeanException {
        String canonical = resolveAlias(beanName);
        BeanDefinition def = beanDefinitionMap.get(canonical);
        if (def == null) {
            if (singletonObjects.containsKey(canonical)) {
                return true;
            }
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return def.scope() == BeanScope.SINGLETON;
    }

    @Override
    public boolean isPrototype(String beanName) throws BeanException {
        String canonical = resolveAlias(beanName);
        BeanDefinition def = beanDefinitionMap.get(canonical);
        if (def == null) {
            if (singletonObjects.containsKey(canonical)) {
                return false;
            }
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return def.scope() == BeanScope.PROTOTYPE;
    }

    @Override
    public boolean isTypeMatch(String beanName, Class<?> targetType) throws BeanException {
        Class<?> type = getType(beanName);
        if (type == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return targetType.isAssignableFrom(type);
    }

    @Override
    @Nullable
    public Class<?> getType(String beanName) {
        String canonical = resolveAlias(beanName);
        BeanDefinition def = beanDefinitionMap.get(canonical);
        if (def != null) {
            return def.type();
        }
        Object singleton = singletonObjects.get(canonical);
        if (singleton != null) {
            return singleton.getClass();
        }
        return null;
    }

    @Override
    public String[] getAliases(String beanName) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            if (entry.getValue().equals(beanName)) {
                result.add(entry.getKey());
            }
        }
        return result.toArray(new String[0]);
    }

    // ===================== ConfigurableBeanFactory =====================

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition) throws BeanException {
        checkNotFrozen("registerBeanDefinition");
        if (beanDefinitionMap.containsKey(beanDefinition.name())) {
            throw new BeanDefinitionStoreException(
                    "Bean definition already exists for name: " + beanDefinition.name());
        }
        beanDefinitionMap.put(beanDefinition.name(), beanDefinition);
        logger.debug("Registered bean definition: {}", beanDefinition.name());
    }

    @Override
    public void removeBeanDefinition(String beanName) throws BeanException {
        checkNotFrozen("removeBeanDefinition");
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        beanDefinitionMap.remove(beanName);
        singletonObjects.remove(beanName);
        logger.debug("Removed bean definition: {}", beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws BeanException {
        BeanDefinition def = beanDefinitionMap.get(beanName);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return def;
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().type())) {
                result.add(entry.getKey());
            }
        }
        // 同时检查通过 registerSingleton 注册的、没有 BeanDefinition 的单例
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            if (!beanDefinitionMap.containsKey(entry.getKey())
                    && type.isAssignableFrom(entry.getValue().getClass())) {
                result.add(entry.getKey());
            }
        }
        return result.toArray(new String[0]);
    }

    @Override
    public void registerAlias(String beanName, String alias) throws BeanException {
        checkNotFrozen("registerAlias");
        if (aliasMap.containsKey(alias)) {
            throw new BeanDefinitionStoreException("Alias already in use: " + alias);
        }
        aliasMap.put(alias, beanName);
        logger.debug("Registered alias '{}' -> '{}'", alias, beanName);
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws BeanException {
        checkNotFrozen("registerSingleton");
        singletonObjects.put(beanName, singletonObject);
        logger.debug("Registered singleton: {}", beanName);
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) throws BeanException {
        checkNotFrozen("addBeanPostProcessor");
        beanPostProcessors.add(beanPostProcessor);
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    @Override
    public void preInstantiateSingletons() throws BeanException {
        for (BeanDefinition def : beanDefinitionMap.values()) {
            if (def.scope() == BeanScope.SINGLETON && !def.lazy()) {
                getBean(def.name());
            }
        }
    }

    @Override
    public void freezeConfiguration() {
        this.configurationFrozen = true;
        logger.debug("Bean factory configuration frozen");
    }

    @Override
    public boolean isConfigurationFrozen() {
        return configurationFrozen;
    }

    @Override
    public void destroySingletons() throws BeanException {
        List<Map.Entry<String, Object>> entries = new ArrayList<>(singletonObjects.entrySet());
        for (Map.Entry<String, Object> entry : entries) {
            doDestroyBean(entry.getKey(), entry.getValue());
        }
        singletonObjects.clear();
        logger.debug("All singletons destroyed");
    }

    // ===================== ListableBeanFactory =====================

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeanException {
        String[] names = getBeanNamesForType(type);
        Map<String, T> result = new LinkedHashMap<>();
        for (String name : names) {
            result.put(name, getBean(name, type));
        }
        return result;
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            if (entry.getValue().type().isAnnotationPresent(annotationType)) {
                result.add(entry.getKey());
            }
        }
        return result.toArray(new String[0]);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeanException {
        String[] names = getBeanNamesForAnnotation(annotationType);
        Map<String, Object> result = new LinkedHashMap<>();
        for (String name : names) {
            result.put(name, getBean(name));
        }
        return result;
    }

    @Override
    @Nullable
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws BeanException {
        Class<?> type = getType(beanName);
        if (type == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return type.getAnnotation(annotationType);
    }

    // ===================== AutowireCapableBeanFactory =====================

    @Override
    public <T> T createBean(Class<T> beanClass) throws BeanException {
        try {
            var ctor = beanClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            T instance = beanClass.cast(ctor.newInstance());
            autowireBean(instance);
            Object result = doInitializeBean(instance, beanClass.getSimpleName(), null);
            return beanClass.cast(result);
        } catch (BeanException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanCreationException(beanClass.getSimpleName(), "Failed to instantiate", e);
        }
    }

    @Override
    public void autowireBean(Object existingBean) throws BeanException {
        injectFieldDependencies(existingBean, existingBean.getClass().getSimpleName());
    }

    @Override
    public Object configureBean(Object existingBean, String beanName) throws BeanException {
        injectFieldDependencies(existingBean, beanName);
        return doInitializeBean(existingBean, beanName, null);
    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) throws BeanException {
        return doInitializeBean(existingBean, beanName, null);
    }

    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeanException {
        Object result = existingBean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            Object processed = processor.postProcessBeforeInitialization(result, beanName);
            if (processed == null) {
                return result;
            }
            result = processed;
        }
        return result;
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeanException {
        Object result = existingBean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            Object processed = processor.postProcessAfterInitialization(result, beanName);
            if (processed == null) {
                return result;
            }
            result = processed;
        }
        return result;
    }

    @Override
    public void destroyBean(Object existingBean) throws BeanException {
        doDestroyBean(existingBean.getClass().getSimpleName(), existingBean);
    }

    @Override
    @Nullable
    public Object resolveDependency(Class<?> dependencyType, @Nullable String requestingBeanName) throws BeanException {
        String[] names = getBeanNamesForType(dependencyType);
        if (names.length == 0) {
            return null;
        }
        // 过滤掉请求者本身（避免自注入）
        List<String> candidates = new ArrayList<>();
        for (String name : names) {
            if (!name.equals(requestingBeanName)) {
                candidates.add(name);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return getBean(candidates.get(0));
        }
        // 多个候选：优先选 primary
        for (String name : candidates) {
            BeanDefinition def = beanDefinitionMap.get(name);
            if (def != null && def.primary()) {
                return getBean(name);
            }
        }
        return getBean(candidates.get(0));
    }

    // ===================== BeanDefinitionRegistry =====================

    @Override
    public boolean isBeanNameInUse(String beanName) {
        return beanDefinitionMap.containsKey(beanName)
                || singletonObjects.containsKey(beanName)
                || aliasMap.containsKey(beanName);
    }

    // ===================== 内部辅助方法 =====================

    /** 解析别名，返回正规beanName */
    private String resolveAlias(String name) {
        String canonical = aliasMap.get(name);
        return canonical != null ? canonical : name;
    }

    /** 检查配置是否已冻结，若冻结则抛出异常 */
    private void checkNotFrozen(String operation) throws BeanException {
        if (configurationFrozen) {
            throw new BeanFactoryConfigurationFrozenException(operation);
        }
    }

    /** 统一获取Bean入口，包含单例缓存、依赖解析和创建逻辑 */
    private Object doGetBean(String beanName, @Nullable Class<?> requiredType) throws BeanException {
        String canonical = resolveAlias(beanName);

        // 先查单例缓存
        Object singleton = singletonObjects.get(canonical);
        if (singleton != null) {
            return singleton;
        }

        BeanDefinition def = beanDefinitionMap.get(canonical);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }

        if (def.scope() == BeanScope.SINGLETON) {
            // 再次检查缓存
            singleton = singletonObjects.get(canonical);
            if (singleton != null) {
                return singleton;
            }
            if (currentlyInCreation.contains(canonical)) {
                throw new BeanCurrentlyInCreationException(canonical);
            }
            // 在解析 dependsOn 之前标记为"创建中"，以检测 dependsOn 链中的循环依赖
            currentlyInCreation.add(canonical);
            try {
                for (String dependency : def.dependsOn()) {
                    if (!canonical.equals(dependency)) {
                        getBean(dependency);
                    }
                }
                Object bean = doCreateBean(def);
                singletonObjects.put(canonical, bean);
                return bean;
            } finally {
                currentlyInCreation.remove(canonical);
            }
        } else {
            // 原型：每次都新建（先解析 dependsOn）
            for (String dependency : def.dependsOn()) {
                if (!canonical.equals(dependency)) {
                    getBean(dependency);
                }
            }
            return doCreateBean(def);
        }
    }

    /** 根据BeanDefinition完整创建并初始化一个Bean */
    private Object doCreateBean(BeanDefinition def) throws BeanException {
        logger.debug("Creating bean: {}", def.name());
        Object instance = instantiateBean(def);
        injectFieldDependencies(instance, def.name());
        return doInitializeBean(instance, def.name(), def);
    }

    /** 实例化Bean（构造函数、静态工厂方法或实例工厂方法） */
    private Object instantiateBean(BeanDefinition def) throws BeanException {
        String beanName = def.name();
        try {
            if (def.isStaticFactoryMethod()) {
                Method method = def.type().getDeclaredMethod(
                        def.factoryMethodName(), def.constructorArgTypes());
                method.setAccessible(true);
                Object[] args = resolveArgs(def.constructorArgTypes(), beanName);
                return method.invoke(null, args);
            } else if (def.isInstanceFactoryMethod()) {
                Object factoryBean = getBean(def.factoryBeanName());
                Method method = factoryBean.getClass().getDeclaredMethod(
                        def.factoryMethodName(), def.constructorArgTypes());
                method.setAccessible(true);
                Object[] args = resolveArgs(def.constructorArgTypes(), beanName);
                return method.invoke(factoryBean, args);
            } else {
                // 构造函数实例化
                if (def.constructorArgTypes().length == 0) {
                    var ctor = def.type().getDeclaredConstructor();
                    ctor.setAccessible(true);
                    return ctor.newInstance();
                }
                var ctor = def.type().getDeclaredConstructor(def.constructorArgTypes());
                ctor.setAccessible(true);
                Object[] args = resolveArgs(def.constructorArgTypes(), beanName);
                return ctor.newInstance(args);
            }
        } catch (BeanException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanInstantiationFailedException(beanName, e);
        }
    }

    /** 解析构造函数或工厂方法的参数列表 */
    private Object[] resolveArgs(Class<?>[] argTypes, String requestingBeanName) throws BeanException {
        Object[] args = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            Object resolved = resolveDependency(argTypes[i], requestingBeanName);
            if (resolved == null) {
                throw new UnsatisfiedDependencyException(requestingBeanName, null, argTypes[i]);
            }
            args[i] = resolved;
        }
        return args;
    }

    /**
     * 按类型注入Bean字段依赖（跳过基本类型和标准Java类型）。
     * 仅注入工厂中能找到匹配Bean的字段。
     */
    private void injectFieldDependencies(Object bean, String beanName) throws BeanException {
        Class<?> clazz = bean.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!isInjectableType(field.getType())) {
                    continue;
                }
                Object dependency = resolveDependency(field.getType(), beanName);
                if (dependency != null) {
                    try {
                        field.setAccessible(true);
                        field.set(bean, dependency);
                    } catch (IllegalAccessException e) {
                        throw new BeanDependencyInjectionException(
                                beanName, field.getName(), "Cannot set field value", e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 判断某个类型是否可作为依赖注入目标
     * （排除原始类型、数组，以及标准Java库类型）
     */
    private boolean isInjectableType(Class<?> type) {
        if (type.isPrimitive() || type.isArray()) {
            return false;
        }
        String name = type.getName();
        return !name.startsWith("java.") && !name.startsWith("javax.");
    }

    /** 完整初始化流程：Aware回调 -> 前置处理器 -> init方法 -> 后置处理器 */
    private Object doInitializeBean(Object bean, String beanName, @Nullable BeanDefinition def) throws BeanException {
        invokeAwareMethods(bean, beanName);
        Object result = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
        invokeInitMethods(result, beanName, def);
        result = applyBeanPostProcessorsAfterInitialization(result, beanName);
        return result;
    }

    /** 调用 Aware 接口回调 */
    private void invokeAwareMethods(Object bean, String beanName) throws BeanException {
        if (bean instanceof BeanNameAware aware) {
            try {
                aware.setBeanName(beanName);
            } catch (Exception e) {
                throw new BeanAwareProcessException(beanName, BeanNameAware.class, e);
            }
        }
        if (bean instanceof BeanClassLoaderAware aware) {
            try {
                aware.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
            } catch (Exception e) {
                throw new BeanAwareProcessException(beanName, BeanClassLoaderAware.class, e);
            }
        }
        if (bean instanceof BeanFactoryAware aware) {
            try {
                aware.setBeanFactory(this);
            } catch (BeanException e) {
                throw new BeanAwareProcessException(beanName, BeanFactoryAware.class, e);
            }
        }
    }

    /** 调用初始化方法（InitializingBean.afterPropertiesSet 和自定义 initMethodName） */
    private void invokeInitMethods(Object bean, String beanName, @Nullable BeanDefinition def) throws BeanException {
        if (bean instanceof InitializingBean initBean) {
            try {
                initBean.afterPropertiesSet();
            } catch (BeanException e) {
                throw new BeanInitializationException(beanName, e);
            }
        }
        if (def != null && def.initMethodName() != null) {
            try {
                Method initMethod = bean.getClass().getDeclaredMethod(def.initMethodName());
                initMethod.setAccessible(true);
                initMethod.invoke(bean);
            } catch (Exception e) {
                throw new BeanInitializationException(beanName,
                        "Failed to invoke init method '" + def.initMethodName() + "'", e);
            }
        }
    }

    /** 销毁单个Bean（DisposableBean.destroy 和自定义 destroyMethodName） */
    private void doDestroyBean(String beanName, Object bean) throws BeanException {
        BeanDefinition def = beanDefinitionMap.get(beanName);
        if (bean instanceof DisposableBean disposable) {
            try {
                disposable.destroy();
            } catch (BeanException e) {
                throw new BeanDestructionException(beanName, e);
            }
        }
        if (def != null && def.destroyMethodName() != null) {
            try {
                Method destroyMethod = bean.getClass().getDeclaredMethod(def.destroyMethodName());
                destroyMethod.setAccessible(true);
                destroyMethod.invoke(bean);
            } catch (Exception e) {
                throw new BeanDestructionException(beanName,
                        "Failed to invoke destroy method '" + def.destroyMethodName() + "'", e);
            }
        }
    }
}
