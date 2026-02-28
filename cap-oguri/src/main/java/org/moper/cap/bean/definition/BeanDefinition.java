package org.moper.cap.bean.definition;

/**
 * Bean 的完整元数据描述（不可变）。
 *
 * <p>通过静态工厂方法 {@link #of(String, Class)} 创建实例，支持链式调用。
 * 如需修改行为性字段，使用对应的 {@code with*} 方法返回新实例，原实例不受影响。
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 最简单的单例 Bean（无参构造函数实例化）
 * BeanDefinition def = BeanDefinition.of("userService", UserService.class);
 *
 * // 链式配置
 * BeanDefinition def = BeanDefinition.of("dataSource", DataSource.class)
 *     .withInstantiationPolicy(InstantiationPolicy.staticFactory("create", Config.class))
 *     .dependsOn("configBean")
 *     .withPrimary(true)
 *     .withLazy(true);
 *
 * // 运行期调整行为性字段（容器冻结前）
 * BeanDefinition updated = original.withLazy(true);
 * registry.registerBeanDefinition(updated);
 * }</pre>
 *
 * @param name                Bean 的唯一标识名称，不能为空
 * @param type                Bean 的类型，不能为 null
 * @param scope               Bean 的作用域，不能为 null，默认 {@link BeanScope#SINGLETON}
 * @param instantiationPolicy Bean 的实例化策略，不能为 null，
 *                            描述容器如何创建该 Bean 的实例，
 *                            默认为无参构造函数策略
 * @param dependsOn           强依赖的其他 Bean 名称，不能为 null，
 *                            容器保证这些 Bean 先于本 Bean 初始化，
 *                            适用于非注入式的顺序依赖，默认为空数组
 * @param lazy                是否延迟初始化，{@code true} 表示首次 {@code getBean}
 *                            时才创建实例，仅对 {@link BeanScope#SINGLETON} 有效，
 *                            默认 {@code false}
 * @param primary             是否为同类型 Bean 的首选候选，按类型查找时若存在多个匹配，
 *                            优先返回标记为 {@code primary} 的 Bean，默认 {@code false}
 * @param description         可读描述信息，不能为 null，默认为空字符串
 */
public record BeanDefinition(
        String name,
        Class<?> type,
        BeanScope scope,
        InstantiationPolicy instantiationPolicy,
        String[] dependsOn,
        boolean lazy,
        boolean primary,
        String description
) {

    public BeanDefinition{
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bean name must not be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Bean type must not be null");
        }
    }

    /**
     * 创建一个以无参构造函数实例化的单例 BeanDefinition。
     *
     * <p>所有可选字段均使用默认值：
     * {@code scope=SINGLETON, lazy=false, primary=false, autowired=true, description=""}
     *
     * @param name Bean 唯一名称，不能为空
     * @param type Bean 类型，不能为 null
     * @return 新的 BeanDefinition 实例
     * @throws IllegalArgumentException 如果 name 为空或 type 为 null
     */
    public static BeanDefinition of(String name, Class<?> type) {
        return new BeanDefinition(
                name, type, BeanScope.SINGLETON,
                InstantiationPolicy.constructor(),
                new String[0],
                false, false, "");
    }

    /**
     * 返回一个使用指定实例化策略的新 BeanDefinition。
     *
     * @param policy 实例化策略，不能为 null
     * @return 新的 BeanDefinition 实例
     * @see InstantiationPolicy#constructor(Class[])
     * @see InstantiationPolicy#staticFactory(String, Class[])
     * @see InstantiationPolicy#instanceFactory(String, String, Class[])
     */
    public BeanDefinition withInstantiationPolicy(InstantiationPolicy policy) {
        return new BeanDefinition(
                name, type, scope, policy,
                dependsOn, lazy, primary, description);
    }

    /**
     * 返回一个使用指定作用域的新 BeanDefinition。
     *
     * @param scope 作用域，不能为 null
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withScope(BeanScope scope) {
        return new BeanDefinition(
                name, type, scope, instantiationPolicy,
                dependsOn, lazy, primary, description);
    }

    /**
     * 返回一个使用指定 dependsOn 的新 BeanDefinition。
     *
     * @param beanNames 强依赖的 Bean 名称，不能为 null
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition dependsOn(String... beanNames) {
        return new BeanDefinition(
                name, type, scope, instantiationPolicy,
                beanNames, lazy, primary, description);
    }

    /**
     * 返回一个 {@code lazy} 字段被修改的新 BeanDefinition。
     *
     * @param lazy 是否延迟初始化
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withLazy(boolean lazy) {
        return new BeanDefinition(
                name, type, scope, instantiationPolicy,
                dependsOn, lazy, primary, description);
    }

    /**
     * 返回一个 {@code primary} 字段被修改的新 BeanDefinition。
     *
     * @param primary 是否为首选候选
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withPrimary(boolean primary) {
        return new BeanDefinition(
                name, type, scope, instantiationPolicy,
                dependsOn, lazy, primary, description);
    }


    /**
     * 返回一个 {@code description} 字段被修改的新 BeanDefinition。
     *
     * @param description 描述信息，不能为 null
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withDescription(String description) {
        return new BeanDefinition(
                name, type, scope, instantiationPolicy,
                dependsOn, lazy, primary, description);
    }

    /**
     * 是否为单例作用域。
     *
     * @return {@code scope == SINGLETON}
     */
    public boolean isSingleton() {
        return scope == BeanScope.SINGLETON;
    }

    /**
     * 是否为原型作用域。
     *
     * @return {@code scope == PROTOTYPE}
     */
    public boolean isPrototype() {
        return scope == BeanScope.PROTOTYPE;
    }
}