package org.moper.cap.bean.container.impl;

import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.container.BeanCreationEngine;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.exception.*;
import org.moper.cap.bean.interceptor.BeanInterceptor;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link BeanContainer} 的默认实现。
 *
 * <p>{@code DefaultBeanContainer} 只负责存储和执行，不负责生命周期编排：
 * <ul>
 *   <li>配置冻结逻辑由 {@code BootstrapContext} 实现，容器本身不感知冻结状态</li>
 *   <li>预实例化和销毁的触发时机由上层组件决定，容器只提供执行能力</li>
 * </ul>
 *
 * <p><b>内部数据结构：</b>
 * <ul>
 *   <li>{@code beanDefinitionMap}：BeanDefinition 注册表，key = beanName，保持注册顺序</li>
 *   <li>{@code singletonObjects}：单例缓存，key = beanName，线程安全</li>
 *   <li>{@code aliasMap}：别名映射，key = alias，value = beanName</li>
 *   <li>{@code currentlyInCreation}：正在创建中的 Bean 名称集合，用于循环依赖检测</li>
 * </ul>
 */
public class DefaultBeanContainer implements BeanContainer {

    private final Map<String, BeanDefinition> beanDefinitionMap   = new LinkedHashMap<>();
    private final Map<String, Object>         singletonObjects    = new ConcurrentHashMap<>();
    private final Map<String, String>         aliasMap            = new ConcurrentHashMap<>();
    private final Set<String>                 currentlyInCreation = Collections.synchronizedSet(new HashSet<>());

    private final BeanCreationEngine creationEngine = new DefaultBeanCreationEngine(this);

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        String name = beanDefinition.name();
        if (beanDefinitionMap.containsKey(name)) {
            throw new BeanDefinitionStoreException("Bean definition with name '" + name + "' already exists.");
        }
        beanDefinitionMap.put(name, beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        beanDefinitionMap.remove(beanName);
        singletonObjects.remove(beanName);
    }

    @Override
    public void registerAlias(String beanName, String alias) throws NoSuchBeanDefinitionException, BeanDefinitionStoreException {
        if (!beanDefinitionMap.containsKey(beanName) && !singletonObjects.containsKey(beanName)) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        if (aliasMap.containsKey(alias) || beanDefinitionMap.containsKey(alias)) {
            throw new BeanDefinitionStoreException("Alias '" + alias + "' is already in use");
        }
        aliasMap.put(alias, beanName);
    }

    @Override
    public void removeAlias(String alias) throws BeanDefinitionStoreException {
        if (!aliasMap.containsKey(alias)) {
            throw new BeanDefinitionStoreException("Alias '" + alias + "' is not registered");
        }
        aliasMap.remove(alias);
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws BeanDefinitionStoreException {
        if (singletonObjects.containsKey(beanName) || beanDefinitionMap.containsKey(beanName)) {
            throw new BeanDefinitionStoreException("Name '" + beanName + "' is already in use");
        }
        singletonObjects.put(beanName, singletonObject);
    }

    @Override
    public boolean isBeanNameInUse(String beanName) {
        return beanDefinitionMap.containsKey(beanName) || singletonObjects.containsKey(beanName) || aliasMap.containsKey(beanName);
    }

    @Override
    public Object getBean(String beanName) throws NoSuchBeanDefinitionException, BeanCreationException {
        return doGetBean(beanName, null);
    }

    @Override
    public <T> T getBean(String beanName, Class<T> requiredType) throws NoSuchBeanDefinitionException, BeanCreationException, BeanNotOfRequiredTypeException {
        Object bean = doGetBean(beanName, requiredType);
        if (!requiredType.isInstance(bean)) {
            throw new BeanNotOfRequiredTypeException(beanName, requiredType, bean.getClass());
        }
        return requiredType.cast(bean);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws NoSuchBeanDefinitionException, BeanCreationException, NoUniqueBeanDefinitionException {
        String[] names = getBeanNamesForType(requiredType);
        if (names.length == 0) {
            throw new NoSuchBeanDefinitionException(requiredType);
        }
        if (names.length == 1) {
            return getBean(names[0], requiredType);
        }
        List<String> primaryCandidates = Arrays.stream(names)
                .filter(name -> {
                    BeanDefinition def = beanDefinitionMap.get(resolveAlias(name));
                    return def != null && def.primary();
                })
                .toList();
        if (primaryCandidates.size() == 1) {
            return getBean(primaryCandidates.get(0), requiredType);
        }
        throw new NoUniqueBeanDefinitionException(requiredType, names);
    }

    private Object doGetBean(String beanName, Class<?> requiredType) throws NoSuchBeanDefinitionException, BeanCreationException {
        String resolvedName = resolveAlias(beanName);

        // 1. 单例缓存命中
        Object singleton = singletonObjects.get(resolvedName);
        if (singleton != null) {
            return singleton;
        }

        // 2. 查找 BeanDefinition
        BeanDefinition def = beanDefinitionMap.get(resolvedName);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }

        // 3. 处理 dependsOn（确保依赖先于本 Bean 初始化）
        for (String dep : def.dependsOn()) {
            getBean(dep);
        }

        // 4. 按作用域创建
        return switch (def.scope()) {
            case SINGLETON -> getOrCreateSingleton(resolvedName, def);
            case PROTOTYPE -> creationEngine.createBean(resolvedName, def);
            default -> throw new BeanCreationException(resolvedName,
                    "Scope '" + def.scope() + "' is not supported by cap-bean kernel. " +
                            "Web scopes (REQUEST/SESSION) are handled by cap-web.");
        };
    }

    private Object getOrCreateSingleton(String beanName, BeanDefinition def) throws BeanCreationException {
        Object bean = singletonObjects.get(beanName);
        if (bean != null) {
            return bean;
        }
        synchronized (singletonObjects) {
            bean = singletonObjects.get(beanName);
            if (bean != null) {
                return bean;
            }
            if (!currentlyInCreation.add(beanName)) {
                throw new BeanCreationException(beanName,
                        "Circular dependency detected: bean '" + beanName +
                                "' is currently being created");
            }
            try {
                bean = creationEngine.createBean(beanName, def);
                singletonObjects.put(beanName, bean);
            } finally {
                currentlyInCreation.remove(beanName);
            }
        }
        return bean;
    }

    @Override
    public boolean containsBean(String beanName) {
        String resolved = resolveAlias(beanName);
        return beanDefinitionMap.containsKey(resolved) || singletonObjects.containsKey(resolved);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        String resolved = resolveAlias(beanName);
        return beanDefinitionMap.containsKey(resolved);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        String resolved = resolveAlias(beanName);
        BeanDefinition def = beanDefinitionMap.get(resolved);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(beanName); // 错误信息保留原始入参，便于排查
        }
        return def;
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(String[]::new);
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> result = new ArrayList<>();

        // 1. 遍历 beanDefinitionMap
        beanDefinitionMap.entrySet().stream()
                .filter(e -> type.isAssignableFrom(e.getValue().type()))
                .map(Map.Entry::getKey)
                .forEach(result::add);

        // 2. 遍历 singletonObjects（registerSingleton 注册的外部单例）
        //    排除已在 beanDefinitionMap 中存在的名称，避免重复
        singletonObjects.entrySet().stream()
                .filter(e -> !beanDefinitionMap.containsKey(e.getKey()))
                .filter(e -> type.isAssignableFrom(e.getValue().getClass()))
                .map(Map.Entry::getKey)
                .forEach(result::add);

        return result.toArray(String[]::new);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        List<String> result = new ArrayList<>();

        // 1. 遍历 beanDefinitionMap
        beanDefinitionMap.entrySet().stream()
                .filter(e -> e.getValue().type().isAnnotationPresent(annotationType))
                .map(Map.Entry::getKey)
                .forEach(result::add);

        // 2. 遍历 singletonObjects
        singletonObjects.entrySet().stream()
                .filter(e -> !beanDefinitionMap.containsKey(e.getKey()))
                .filter(e -> e.getValue().getClass().isAnnotationPresent(annotationType))
                .map(Map.Entry::getKey)
                .forEach(result::add);

        return result.toArray(String[]::new);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeanCreationException {
        Map<String, T> result = new LinkedHashMap<>();
        for (String name : getBeanNamesForType(type)) {
            result.put(name, getBean(name, type));
        }
        return result;
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeanCreationException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String name : getBeanNamesForAnnotation(annotationType)) {
            result.put(name, getBean(name));
        }
        return result;
    }

    @Override
    public boolean isSingleton(String beanName) throws NoSuchBeanDefinitionException {
        String resolved = resolveAlias(beanName);
        return getBeanDefinition(resolved).isSingleton();
    }

    @Override
    public boolean isPrototype(String beanName) throws NoSuchBeanDefinitionException {
        String resolved = resolveAlias(beanName);
        return getBeanDefinition(resolved).isPrototype();
    }

    @Override
    public boolean isTypeMatch(String beanName, Class<?> targetType) throws NoSuchBeanDefinitionException {
        Class<?> type = getType(beanName);
        return type != null && targetType.isAssignableFrom(type);
    }

    @Override
    public Class<?> getType(String beanName) throws NoSuchBeanDefinitionException {
        String resolved = resolveAlias(beanName);
        Object singleton = singletonObjects.get(resolved);
        if (singleton != null) {
            return singleton.getClass();
        }
        BeanDefinition def = beanDefinitionMap.get(resolved);
        if(def == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }else return def.type();
    }

    @Override
    public String[] getAliases(String beanName) throws NoSuchBeanDefinitionException {
        String resolved = resolveAlias(beanName);

        if(!beanDefinitionMap.containsKey(resolved) && !singletonObjects.containsKey(resolved)) {
            throw new NoSuchBeanDefinitionException(beanName);
        }

        return aliasMap.entrySet().stream()
                .filter(e -> e.getValue().equals(resolved))
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(String beanName,
                                                                   Class<A> annotationType) throws NoSuchBeanDefinitionException {
        Class<?> type = getType(beanName);
        if (type == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return type.getAnnotation(annotationType);
    }

    @Override
    public void addBeanInterceptor(BeanInterceptor interceptor) {
        creationEngine.addBeanInterceptor(interceptor);
    }

    /**
     * 获取所有已注册的拦截器（只读视图，已按 {@link BeanInterceptor#getOrder()} 升序排列）。
     *
     * @return 不可变的拦截器列表，永不为 null
     */
    @Override
    public List<BeanInterceptor> getBeanInterceptors() {
        return creationEngine.getBeanInterceptors();
    }

    /**
     * 获取已注册的拦截器数量。
     *
     * @return 拦截器数量，最小为 0
     */
    @Override
    public int getBeanInterceptorCount() {
        return creationEngine.getBeanInterceptorCount();
    }

    @Override
    public void preInstantiateSingletons() throws BeanCreationException {
        List<String> names = new ArrayList<>(beanDefinitionMap.keySet());
        for (String name : names) {
            BeanDefinition def = beanDefinitionMap.get(name);
            if (def.isSingleton() && !def.lazy()) {
                getBean(name);
            }
        }
    }

    @Override
    public void destroyBean(String beanName) throws BeanDestructionException {
        String resolved = resolveAlias(beanName);
        creationEngine.destroyBean(resolved);
        singletonObjects.remove(resolved);
    }

    @Override
    public void destroySingletons() throws BeanDestructionException {
        creationEngine.destroyAllSingletons();
        singletonObjects.clear();
    }

    private String resolveAlias(String nameOrAlias) {
        return aliasMap.getOrDefault(nameOrAlias, nameOrAlias);
    }
}