package org.moper.cap.bean.definition;

import org.moper.cap.bean.exception.BeanDefinitionStoreException;

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
 * // 构造函数注入
 * BeanDefinition def = BeanDefinition.of("orderService", OrderService.class)
 *     .withParameterBeanNames(new String[]{"userService", "configBean"});
 *
 * // 工厂方法实例化
 * BeanDefinition def = BeanDefinition.of("dataSource", DataSource.class)
 *     .withFactoryMethod("dataSourceFactory", "create")
 *     .withParameterBeanNames(new String[]{"configBean"})
 *     .dependsOn("configBean")
 *     .withPrimary(true)
 *     .withLazy(true);
 *
 * // 运行期调整行为性字段（容器冻结前）
 * BeanDefinition updated = original.withLazy(true);
 * registry.registerBeanDefinition(updated);
 * }</pre>
 *
 * @param name               Bean 的唯一标识名称，不能为空
 * @param type               Bean 的类型，不能为 null
 * @param scope              Bean 的作用域，不能为 null，默认 {@link BeanScope#SINGLETON}
 * @param dependsOn          强依赖的其他 Bean 名称，不能为 null，
 *                           容器保证这些 Bean 先于本 Bean 初始化，
 *                           适用于非注入式的顺序依赖，默认为空数组
 * @param lazy               是否延迟初始化，{@code true} 表示首次 {@code getBean}
 *                           时才创建实例，仅对 {@link BeanScope#SINGLETON} 有效，
 *                           默认 {@code false}
 * @param primary            是否为同类型 Bean 的首选候选，按类型查找时若存在多个匹配，
 *                           优先返回标记为 {@code primary} 的 Bean，默认 {@code false}
 * @param description        可读描述信息，不能为 null，默认为空字符串
 * @param parameterBeanNames 构造函数或工厂方法参数的 Bean 名称列表，空数组表示无参，不能为 null
 * @param factoryMethodName  工厂方法名称，null 表示非工厂方法实例化；
 *                           必须与 factoryBeanName 同时为 null 或同时非 null
 * @param factoryBeanName    工厂 Bean 的名称，null 表示非工厂方法实例化；
 *                           必须与 factoryMethodName 同时为 null 或同时非 null
 */
public record BeanDefinition(
        String name,
        Class<?> type,
        BeanScope scope,
        String[] dependsOn,
        boolean lazy,
        boolean primary,
        String description,
        String[] parameterBeanNames,
        String factoryMethodName,
        String factoryBeanName
) {

    public BeanDefinition {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bean name must not be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Bean type must not be null");
        }
        if (parameterBeanNames == null) {
            throw new IllegalArgumentException("parameterBeanNames must not be null");
        }
        if ((factoryBeanName == null) != (factoryMethodName == null)) {
            throw new BeanDefinitionStoreException(
                    "factoryBeanName and factoryMethodName must both be null or both non-null, " +
                    "but got factoryBeanName='" + factoryBeanName + "' and factoryMethodName='" + factoryMethodName + "'");
        }
    }

    /**
     * 创建一个以无参构造函数实例化的单例 BeanDefinition。
     *
     * <p>所有可选字段均使用默认值：
     * {@code scope=SINGLETON, lazy=false, primary=false, description=""}
     *
     * @param name Bean 唯一名称，不能为空
     * @param type Bean 类型，不能为 null
     * @return 新的 BeanDefinition 实例
     * @throws IllegalArgumentException 如果 name 为空或 type 为 null
     */
    public static BeanDefinition of(String name, Class<?> type) {
        return new BeanDefinition(
                name, type, BeanScope.SINGLETON,
                new String[0],
                false, false, "",
                new String[0], null, null);
    }

    /**
     * 返回一个使用指定参数 Bean 名称的新 BeanDefinition。
     *
     * <p>适用于构造函数注入和工厂方法注入，参数按顺序排列。
     *
     * @param parameterBeanNames 参数的 Bean 名称，按顺序排列
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withParameterBeanNames(String[] parameterBeanNames) {
        String[] names = parameterBeanNames == null ? new String[0] : parameterBeanNames;
        return new BeanDefinition(
                name, type, scope, dependsOn, lazy, primary, description,
                names, factoryMethodName, factoryBeanName);
    }

    /**
     * 返回一个使用指定工厂方法实例化的新 BeanDefinition。
     *
     * <p>如需传入工厂方法参数，请在调用此方法后链式调用
     * {@link #withParameterBeanNames(String[])}。
     *
     * @param factoryBeanName   工厂 Bean 的名称，不能为空
     * @param factoryMethodName 工厂方法名称，不能为空
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withFactoryMethod(String factoryBeanName, String factoryMethodName) {
        return new BeanDefinition(
                name, type, scope, dependsOn, lazy, primary, description,
                parameterBeanNames, factoryMethodName, factoryBeanName);
    }

    /**
     * 返回一个使用指定作用域的新 BeanDefinition。
     *
     * @param scope 作用域，不能为 null
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withScope(BeanScope scope) {
        return new BeanDefinition(
                name, type, scope, dependsOn, lazy, primary, description,
                parameterBeanNames, factoryMethodName, factoryBeanName);
    }

    /**
     * 返回一个使用指定 dependsOn 的新 BeanDefinition。
     *
     * @param beanNames 强依赖的 Bean 名称，不能为 null
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition dependsOn(String... beanNames) {
        return new BeanDefinition(
                name, type, scope, beanNames, lazy, primary, description,
                parameterBeanNames, factoryMethodName, factoryBeanName);
    }

    /**
     * 返回一个 {@code lazy} 字段被修改的新 BeanDefinition。
     *
     * @param lazy 是否延迟初始化
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withLazy(boolean lazy) {
        return new BeanDefinition(
                name, type, scope, dependsOn, lazy, primary, description,
                parameterBeanNames, factoryMethodName, factoryBeanName);
    }

    /**
     * 返回一个 {@code primary} 字段被修改的新 BeanDefinition。
     *
     * @param primary 是否为首选候选
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withPrimary(boolean primary) {
        return new BeanDefinition(
                name, type, scope, dependsOn, lazy, primary, description,
                parameterBeanNames, factoryMethodName, factoryBeanName);
    }

    /**
     * 返回一个 {@code description} 字段被修改的新 BeanDefinition。
     *
     * @param description 描述信息，不能为 null
     * @return 新的 BeanDefinition 实例
     */
    public BeanDefinition withDescription(String description) {
        return new BeanDefinition(
                name, type, scope, dependsOn, lazy, primary, description,
                parameterBeanNames, factoryMethodName, factoryBeanName);
    }

    /**
     * 是否为工厂方法实例化。
     *
     * @return {@code true} 表示通过工厂方法创建实例
     */
    public boolean isFactoryMethod() {
        return factoryMethodName != null && !factoryMethodName.isBlank();
    }

    /**
     * 是否为构造函数实例化。
     *
     * @return {@code true} 表示通过构造函数创建实例
     */
    public boolean isConstructor() {
        return !isFactoryMethod();
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

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "name='" + name + '\'' +
                ", type=" + type.getName() +
                ", scope=" + scope +
                ", lazy=" + lazy +
                ", primary=" + primary +
                ", description='" + description + '\'' +
                '}';
    }
}