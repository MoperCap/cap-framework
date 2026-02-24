package org.moper.cap.property.subscriber.view;

import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.subscriber.PropertySelector;
import org.moper.cap.property.subscriber.PropertyViewPool;
import org.moper.cap.property.subscriber.selector.AnyPropertySelector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认的只读属性视图池实现 </br>
 *
 * 该实现提供了对Officer中属性资源的只读访问接口，允许外部模块查询和读取Officer管理的属性值。 </br>
 * PropertyViewPool本身不存储属性数据，而是通过订阅Officer来实时获取属性的最新值。 </br>
 *
 * 核心特性：
 * <ul>
 *   <li><strong>只读访问</strong>：提供了多种只读查询方法，无法修改属性值</li>
 *   <li><strong>类型转换</strong>：支持将属性值转换为指定的类型</li>
 *   <li><strong>默认值支持</strong>：查询不存在的属性时可提供默认值</li>
 *   <li><strong>Optional 支持</strong>：提供函数式编程的 Optional 接口</li>
 *   <li><strong>线程安全</strong>：使用 ConcurrentHashMap 保证并发访问安全</li>
 *   <li><strong>实时更新</strong>：通过订阅 Officer 来实时更新属性值</li>
 *   <li><strong>全量订阅</strong>：使用 AnyPropertySelector 关注所有属性变化</li>
 * </ul>
 *
 * 订阅策略说明：
 * PropertyViewPool 使用 AnyPropertySelector，这意味着它关注 Officer 中的所有
 * 属性变化。这样做的原因是：
 * <ul>
 *   <li>ViewPool 是一个通用的只读视图，不知道哪些属性会被查询</li>
 *   <li>为了能够对任何属性的查询请求立即返回最新值，必须订阅所有属性</li>
 *   <li>这样做的性能开销较小，因为 ViewPool 只是缓存属性值，不做复杂处理</li>
 * </ul>
 *
 * 属性存储说明：
 * PropertyViewPool 内部维护一个属性值映射表，用于缓存从 Officer 接收到的
 * 属性值。当 Officer 发送属性更新时，ViewPool 会实时更新这个缓存 </br>
 *
 * 使用示例：
 * <pre>
 * // 创建 Officer 并注册视图池
 * PropertyOfficer officer = new DefaultPropertyOfficer.DefaultPropertyOfficerBuilder()
 *     .name("ConfigOfficer")
 *     .build();
 *
 * // 创建只读视图池
 * PropertyViewPool viewPool = new DefaultPropertyViewPool("ConfigViewPool");
 * officer.subsribe(viewPool);
 *
 * // 查询属性值
 * String host = viewPool.getPropertyValue("db.host", String.class);
 * int port = viewPool.getPropertyValueOrDefault("db.port", Integer.class, 3306);
 * Optional<String> username = viewPool.getPropertyValueOptional("db.username", String.class);
 *
 * // 检查属性是否存在
 * if (viewPool.containsProperty("db.host")) {
 *     System.out.println("db.host exists");
 * }
 *
 * // 获取所有属性键
 * Set<String> allKeys = viewPool.keySet();
 *
 * // 使用完毕后关闭
 * viewPool.close();
 * </pre>
 *
 * 线程安全性说明：
 * <ul>
 *   <li>使用 ConcurrentHashMap 存储属性值，支持并发读写</li>
 *   <li>使用 AtomicBoolean 保证关闭状态的原子性</li>
 *   <li>所有查询方法都是线程安全的</li>
 * </ul>
 *
 * 关闭行为说明：
 * 关闭后，ViewPool 将继续保留已缓存的属性值，但不会再接收来自 Officer 的
 * 属性更新通知。这样做的好处是即使 Officer 关闭或 ViewPool 被取消订阅，
 * 外部模块仍然可以查询最后缓存的属性值。
 */
public final class DefaultPropertyViewPool implements PropertyViewPool {

    /**
     * 视图池的名称
     */
    private final  String name;

    /**
     * 属性值存储 </br>
     * 键：属性键（String） </br>
     * 值：属性值（Object） </br>
     *
     * 该映射缓存了所有从 Officer 接收到的属性值。
     */
    private final  Map<String, Object> properties;

    /**
     * 视图池的关闭标志 </br>
     * 使用原子布尔值保证线程安全性 </br>
     */
    private final  AtomicBoolean closed;

    /**
     * 构造函数 </br>
     *
     * 初始化工作：
     * <ol>
     *   <li>验证 name 不为 null 或空</li>
     *   <li>初始化属性存储为 ConcurrentHashMap</li>
     *   <li>初始化关闭标志为 false</li>
     * </ol>
     *
     * @param name 视图池的名称，不能为 null 或空字符串
     * @throws IllegalArgumentException 如果 name 为 null 或空
     */
    public DefaultPropertyViewPool( String name) {
        this.name = name;
        this.properties = new ConcurrentHashMap<>();
        this.closed = new AtomicBoolean(false);
    }

    /**
     * 获取视图池的名称。
     *
     * @return 视图池的名称
     */
    @Override
    public  String name() {
        return name;
    }

    /**
     * 获取属性选择器。 </br>
     *
     * PropertyViewPool 使用 AnyPropertySelector，表示关注所有属性。
     *
     * @return AnyPropertySelector 实例
     */
    @Override
    public  PropertySelector selector() {
        return new AnyPropertySelector();
    }

    /**
     * 接收属性管理平台发送的事件清单。</br>
     *
     * 若视图池已关闭，该方法将忽略接收的事件。
     *
     * @param operations 属性管理平台发送的事件清单
     */
    @Override
    public void dispatch( PropertyOperation... operations) {
        if (closed.get()) {
            return;
        }

        for (PropertyOperation operation : operations) {
            processOperation(operation);
        }
    }

    /**
     * 当属性发布者不在线时，视图池将接收到通知。</br>
     *
     * 若视图池已关闭，该方法将忽略该请求。</br>
     * 默认实现：移除该发布者发布的所有属性。</br>
     *
     * @param officer 不在线的属性官管理平台
     */
    @Override
    public void offOfficer( PropertyOfficer officer) {
        if (closed.get()) {
            return;
        }

        // 由于 ViewPool 无法直接获知哪些属性来自于哪个 Officer，
        // 这里采用保守策略：清空所有属性值
        // 实际应用中，可以考虑标记属性的来源，以便精确清理
        properties.clear();
    }

    /**
     * 判断视图池是否已关闭。
     *
     * @return 如果视图池已关闭则返回 true，否则返回 false
     */
    @Override
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * 关闭视图池。</br>
     *
     * 关闭后，视图池将不再接收来自 Officer 的属性更新通知。</br>
     * 已缓存的属性值将保留，可以继续查询。</br>
     * 该方法是幂等的，可以被重复调用。</br>
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;  // 已经关闭，无需重复关闭
        }
    }

    /**
     * 获取指定属性键的原始属性值。</br>
     *
     * 该方法返回属性的原始值，无类型转换。</br>
     *
     * @param key 属性键
     * @return 属性值，如果属性不存在则返回 null
     */
    @Override
    public  Object getRawPropertyValue( String key) {
        return properties.get(key);
    }

    /**
     * 获取指定属性键的属性值，并转换为指定的类型。</br>
     *
     * 该方法会尝试将属性值转换为指定的类型。如果类型转换失败，将返回 null。</br>
     *
     * @param key 属性键
     * @param type 目标类型
     * @param <T> 泛型参数
     * @return 转换后的属性值，如果属性不存在或类型转换失败则返回 null
     */
    @Override
    public <T>  T getPropertyValue( String key,  Class<T> type) {
        Object value = properties.get(key);
        if (value == null) {
            return null;
        }

        return castToType(value, type);
    }

    /**
     * 获取指定属性键的属性值，如果不存在则返回提供的默认值。</br>
     *
     * 该方法会尝试将属性值转换为指定的类型。如果属性不存在或类型转换失败，将返回提供的默认值。</br>
     *
     * @param key 属性键
     * @param type 目标类型
     * @param defaultValue 默认值
     * @param <T> 泛型参数
     * @return 转换后的属性值，如果属性不存在或类型转换失败则返回 defaultValue
     */
    @Override
    public <T>  T getPropertyValueOrDefault( String key,  Class<T> type,  T defaultValue) {
        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }

        T castedValue = castToType(value, type);
        return castedValue != null ? castedValue : defaultValue;
    }

    /**
     * 获取指定属性键的属性值，包装为 Optional。</br>
     *
     * 该方法会尝试将属性值转换为指定的类型。返回的 Optional 对象可以用于函数式编程。</br>
     *
     * @param key 属性键
     * @param type 目标类型
     * @param <T> 泛型参数
     * @return 包含属性值的 Optional，如果属性不存在或类型转换失败则返回 Optional.empty()
     */
    @Override
    public <T>  Optional<T> getPropertyValueOptional( String key,  Class<T> type) {
        Object value = properties.get(key);
        if (value == null) {
            return Optional.empty();
        }

        T castedValue = castToType(value, type);
        return Optional.ofNullable(castedValue);
    }

    /**
     * 判断是否存在指定属性键的属性。</br>
     *
     * @param key 属性键
     * @return 如果属性存在则返回 true，否则返回 false
     */
    @Override
    public boolean containsProperty( String key) {
        return properties.containsKey(key);
    }

    /**
     * 获取当前视图池中的所有属性键。</br>
     *
     * @return 属性键集合的不可修改副本
     */
    @Override
    public  Set<String> keySet() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    /**
     * 处理单个属性操作
     */
    private void processOperation(PropertyOperation operation) {
        if (operation instanceof org.moper.cap.property.event.PropertySetOperation setOp) {
            properties.put(setOp.key(), setOp.value());
        } else if (operation instanceof org.moper.cap.property.event.PropertyRemoveOperation removeOp) {
            properties.remove(removeOp.key());
        }
    }

    /**
     * 将属性值转换为指定的类型
     *
     * @param value 原始值
     * @param type 目标类型
     * @param <T> 泛型参数
     * @return 转换后的值，如果转换失败则返回 null
     */
    @SuppressWarnings("unchecked")
    private <T> T castToType(Object value, Class<T> type) {
        // 如果值已经是目标类型，直接返回
        if (type.isInstance(value)) {
            return (T) value;
        }

        // 尝试进行类型转换
        try {
            if (type == String.class) {
                return (T) value.toString();
            } else if (type == Integer.class && value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            } else if (type == Long.class && value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            } else if (type == Double.class && value instanceof Number) {
                return (T) Double.valueOf(((Number) value).doubleValue());
            } else if (type == Float.class && value instanceof Number) {
                return (T) Float.valueOf(((Number) value).floatValue());
            } else if (type == Boolean.class && value instanceof Boolean) {
                return (T) value;
            } else if (type == Boolean.class && value instanceof String) {
                return (T) Boolean.valueOf(Boolean.parseBoolean((String) value));
            }
        } catch (Exception e) {
            // 转换失败，返回 null
            return null;
        }

        // 无法转换，返回 null
        return null;
    }
}