package org.moper.cap.property.subscriber.subcription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PropertyRemoveOperation;
import org.moper.cap.property.event.PropertySetOperation;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.subscriber.PropertySelector;
import org.moper.cap.property.subscriber.PropertySubscriber;
import org.moper.cap.property.subscriber.PropertySubscription;
import org.moper.cap.property.subscriber.selector.ExactPropertySelector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认的属性订阅客户端实现 </br>
 *
 * 该实现提供了基本的属性订阅客户端功能，包括管理多个属性订阅者、
 * 接收和分发属属性更新事件、处理属性发布者离线事件等 </br>
 *
 * 核心特性：
 * <ul>
 *   <li><strong>静态订阅者管理</strong>：订阅者在初始化时确定，之后不支持动态修改</li>
 *   <li><strong>属性选择器灵活配置</strong>：支持显式指定或自动生成 ExactPropertySelector</li>
 *   <li><strong>事件分发</strong>：根据订阅者的兴趣范围，将属性操作分发给相应的订阅者</li>
 *   <li><strong>线程安全</strong>：使用 ConcurrentHashMap 保证并发安全</li>
 *   <li><strong>异常容错</strong>：订阅者处理异常时不中断其他订阅者的处理</li>
 *   <li><strong>生命周期管理</strong>：支持优雅地关闭订阅客户端，关闭后忽略后续事件</li>
 * </ul>
 *
 * 线程安全性说明：
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 管理订阅者及其属性键映射，支持并发访问</li>
 *   <li>使用 {@link AtomicBoolean} 保证关闭状态的原子性</li>
 * </ul>
 *
 * 事件分发流程：
 * <ol>
 *   <li>dispatch() 接收属性操作列表</li>
 *   <li>对每个操作判断其类型（设置或移除）</li>
 *   <li>遍历 subscribers 映射中的所有订阅者，检查其是否关心该属性</li>
 *   <li>为关心该属性的订阅者调用 onSet() 或 onRemoved() 方法</li>
 *   <li>若订阅者抛出异常，捕获异常并记录日志，继续处理其他订阅者</li>
 * </ol>
 *
 * 发布者离线处理流程：
 * <ol>
 *   <li>当属性发布者离线时，Officer 调用 offOfficer() 方法</li>
 *   <li>Subscription 依次调用所有订阅者的 onRemoved() 方法</li>
 *   <li>通知订阅者相关属性已不可用</li>
 *   <li>若订阅者抛出异常，捕获异常但继续通知其他订阅者</li>
 * </ol>
 *
 * 属性选择器说明：
 * <ul>
 *   <li>构造函数1：指定显式的 PropertySelector，用于自定义属性匹配逻辑</li>
 *   <li>构造函数2：自动根据所有订阅者的属性键生成 ExactPropertySelector，仅匹配订阅者关心的属性键</li>
 * </ul>
 *
 * 设计理念：
 * 订阅者只关心属性值的变化，不关心属性来自哪个发布者或上次修改时间。
 * 这些元数据信息由 Officer 内部管理，Subscription 只负责事件的路由和分发 </br>
 *
 * 使用示例：
 * <pre>
 * // 方式1：使用显式指定的选择器
 * List<PropertySubscriber> subscribers = new ArrayList<>();
 * subscribers.add(new MyPropertySubscriber("db.host"));
 * subscribers.add(new MyPropertySubscriber("db.port"));
 *
 * PropertySubscription subscription = new DefaultPropertySubscription(
 *     "DatabaseConfigSubscription",
 *     key -> key.startsWith("db."),  // 显式指定选择器
 *     subscribers
 * );
 *
 * // 方式2：自动生成 ExactPropertySelector
 * PropertySubscription autoSubscription = new DefaultPropertySubscription(
 *     "DatabaseConfigSubscription",
 *     subscribers  // 自动根据订阅者属性键生成 ExactPropertySelector
 * );
 *
 * // 将订阅客户端注册到 Officer
 * officer.subsribe(subscription);
 * </pre>
 */
public final class DefaultPropertySubscription implements PropertySubscription {

    /**
     * 订阅客户端的名称
     */
    private final @NotBlank String name;

    /**
     * 属性选择器：用于确定该订阅客户端关心的属性范围
     */
    private final @NotNull PropertySelector selector;

    /**
     * 订阅者映射表
     * 键：PropertySubscriber（订阅者对象）
     * 值：该订阅者关注的属性键（String）</br>
     *
     * 该映射既作为订阅者的存储容器，又作为属性键的缓存，
     * 避免每次都调用 getPropertyKey() 方法。
     */
    private final @NotNull Map<PropertySubscriber, String> subscribers;

    /**
     * 订阅客户端的关闭标志
     * 使用原子布尔值保证线程安全性
     */
    private final @NotNull AtomicBoolean closed;

    /**
     * 构造函数1：显式指定属性选择器 </br>
     *
     * 该构造函数用于创建具有自定义属性选择器的订阅客户端。 </br>
     *
     * 初始化工作：
     * <ol>
     *   <li>初始化 subscribers 映射，缓存每个订阅者的属性键</li>
     *   <li>初始化 closed 标志为 false</li>
     * </ol>
     *
     * @param name 订阅客户端的名称
     * @param selector 属性选择器，用于确定关心的属性范围
     * @param subscribers 订阅者集合
     */
    public DefaultPropertySubscription(@NotBlank String name, @NotNull PropertySelector selector,
                                       @NotEmpty Collection<PropertySubscriber> subscribers) {
        this.name = name;
        this.selector = selector;
        this.subscribers = new ConcurrentHashMap<>();
        this.closed = new AtomicBoolean(false);

        // 缓存所有订阅者的属性键
        subscribers.forEach(subscriber -> this.subscribers.put(subscriber, subscriber.getPropertyKey()));
    }

    /**
     * 构造函数2：自动生成 ExactPropertySelector </br>
     *
     * 该构造函数根据所有订阅者的属性键自动生成 ExactPropertySelector。
     * 这样做的好处是 Officer 的 selector() 方法返回的选择器与订阅者的属性范围完全匹配。 </br>
     *
     * 初始化工作：
     * <ol>
     *   <li>初始化 subscribers 映射，缓存每个订阅者的属性键</li>
     *   <li>收集所有订阅者的属性键</li>
     *   <li>根据属性键集合创建 ExactPropertySelector</li>
     *   <li>初始化 closed 标志为 false</li>
     * </ol>
     *
     * @param name 订阅客户端的名称
     * @param subscribers 订阅者集合
     */
    public DefaultPropertySubscription(@NotBlank String name, @NotEmpty Collection<PropertySubscriber> subscribers) {
        this.name = name;
        this.subscribers = new ConcurrentHashMap<>();
        this.closed = new AtomicBoolean(false);

        // 收集所有订阅者的属性键
        Set<String> propertyKeys = new ConcurrentSkipListSet<>();
        for (PropertySubscriber subscriber : subscribers) {
            String propertyKey = subscriber.getPropertyKey();
            this.subscribers.put(subscriber, propertyKey);
            propertyKeys.add(propertyKey);
        }

        // 根据属性键集合创建 ExactPropertySelector
        this.selector = new ExactPropertySelector(propertyKeys);
    }

    // ===================== 公开接口实现 =====================

    /**
     * 获取当前属性订阅客户端的名称。
     *
     * @return 当前属性订阅客户端的名称
     */
    @Override
    public @NotBlank String name() {
        return name;
    }

    /**
     * 获取当前属性订阅客户端的属性选择器 </br>
     *
     * 属性选择器在构造时确定，之后保持不变。
     *
     * @return 当前属性订阅客户端的属性选择器（不可变）
     */
    @Override
    public @NotNull PropertySelector selector() {
        return selector;
    }

    /**
     * 接收属性管理平台发送的事件更新操作列表 </br>
     *
     * 如果订阅客户端已关闭，该方法将忽略接收的事件。
     *
     * @param operations 属性管理平台发送的事件清单
     */
    @Override
    public void dispatch(@NotEmpty PropertyOperation... operations) {
        if (closed.get()) {
            return;
        }

        for (PropertyOperation operation : operations) {
            dispatchSingleOperation(operation);
        }
    }

    /**
     * 当属性发布者不在线时，属性管理平台将接收到通知，并进行相应的处理 </br>
     *
     * 当前版本的属性订阅客户端在接收到发布者离线通知时，会依次触发所有订阅者的 onRemoved() 方法，以通知订阅者相关属性已不可用。 </br>
     *
     * @param officer 不在线的属性官管理平台
     */
    @Override
    public void offOfficer(@NotNull PropertyOfficer officer) {
        // 遍历所有订阅者，依次调用 onRemoved() 方法
        for (PropertySubscriber subscriber : subscribers.keySet()) {
            try {
                subscriber.onRemoved();
            } catch (Exception e) {
                // 订阅者处理异常，不中断其他订阅者的处理
                handleSubscriberOfflineException(subscriber, e);
            }
        }
    }

    /**
     * 判断当前订阅客户端是否已关闭。
     *
     * @return 如果订阅客户端已关闭则返回 true，否则返回 false
     */
    @Override
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * 关闭属性订阅客户端 </br>
     *
     * 关闭后，该订阅客户端将不再接收新的事件。
     * 该方法是幂等的，可以被重复调用。
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;  // 已经关闭，无需重复关闭
        }
    }

    /**
     * 分发单个属性操作给相应的订阅者 </br>
     *
     * 该方法遍历 subscribers 映射中的所有订阅者，检查其是否关心该属性。
     * 对于关心该属性的订阅者，调用相应的 onSet() 或 onRemoved() 方法。
     *
     * @param operation 要分发的属性操作
     */
    private void dispatchSingleOperation(PropertyOperation operation) {
        if (operation instanceof PropertySetOperation setOp) {
            dispatchSetOperation(setOp);
        } else if (operation instanceof PropertyRemoveOperation removeOp) {
            dispatchRemoveOperation(removeOp);
        }
    }

    /**
     * 分发属性设置操作给相应的订阅者 </br>
     *
     * 该方法：
     * <ol>
     *   <li>从操作中提取属性键和属性值</li>
     *   <li>遍历 subscribers 映射中的所有订阅者</li>
     *   <li>检查订阅者的属性键是否与操作的属性键匹配</li>
     *   <li>为匹配的订阅者调用 onSet(value) 方法，仅传递属性值</li>
     *   <li>若订阅者抛出异常，捕获异常但继续处理其他订阅者</li>
     * </ol>
     *
     * @param operation 属性设置操作
     */
    private void dispatchSetOperation(PropertySetOperation operation) {
        String key = operation.key();
        Object value = operation.value();

        // 遍历所有订阅者
        for (Map.Entry<PropertySubscriber, String> entry : subscribers.entrySet()) {
            PropertySubscriber subscriber = entry.getKey();
            String subscriberKey = entry.getValue();

            try {
                // 检查订阅者是否关心该属性键
                if (key.equals(subscriberKey)) {
                    // 仅传递属性值，订阅者不关心元数据
                    subscriber.onSet(value);
                }
            } catch (Exception e) {
                // 订阅者处理异常，不中断其他订阅者的处理
                handleSubscriberException(subscriber, operation, e);
            }
        }
    }

    /**
     * 分发属性移除操作给相应的订阅者 </br>
     *
     * 该方法：
     * <ol>
     *   <li>从操作中提取属性键</li>
     *   <li>遍历 subscribers 映射中的所有订阅者</li>
     *   <li>检查订阅者的属性键是否与操作的属性键匹配</li>
     *   <li>为匹配的订阅者调用 onRemoved() 方法</li>
     *   <li>若订阅者抛出异常，捕获异常但继续处理其他订阅者</li>
     * </ol>
     *
     * @param operation 属性移除操作
     */
    private void dispatchRemoveOperation(PropertyRemoveOperation operation) {
        String key = operation.key();

        // 遍历所有订阅者
        for (Map.Entry<PropertySubscriber, String> entry : subscribers.entrySet()) {
            PropertySubscriber subscriber = entry.getKey();
            String subscriberKey = entry.getValue();

            try {
                // 检查订阅者是否关心该属性键
                if (key.equals(subscriberKey)) {
                    // 调用 onRemoved 方法，无需传递任何参数
                    subscriber.onRemoved();
                }
            } catch (Exception e) {
                // 订阅者处理异常，不中断其他订阅者的处理
                handleSubscriberException(subscriber, operation, e);
            }
        }
    }


    /**
     * 处理订阅者在处理属性操作时抛出的异常 </br>
     *
     * 当前的默认实现是记录异常到标准错误流，不中断其他订阅者的处理 </br>
     *
     * 异常日志格式：
     * <pre>
     * [SubscriptionName] Subscriber [SubscriberKey] failed to handle operation [OperationType]: [ErrorMessage]
     * </pre>
     *
     * @param subscriber 抛出异常的订阅者
     * @param operation 正在处理的属性操作
     * @param exception 抛出的异常
     */
    private void handleSubscriberException(PropertySubscriber subscriber, PropertyOperation operation, Exception exception) {
        System.err.println("[" + name + "] Subscriber [" + subscriber.getPropertyKey() + "] " +
                "failed to handle operation [" + operation.getClass().getSimpleName() + "]: " +
                exception.getMessage());
        exception.printStackTrace(System.err);
    }

    /**
     * 处理订阅者在处理发布者离线事件时抛出的异常 </br>
     *
     * 当前的默认实现是记录异常到标准错误流，不中断其他订阅者的处理 </br>
     *
     * 异常日志格式：
     * <pre>
     * [SubscriptionName] Subscriber [SubscriberKey] failed to handle offOfficer event: [ErrorMessage]
     * </pre>
     *
     * @param subscriber 抛出异常的订阅者
     * @param exception 抛出的异常
     */
    private void handleSubscriberOfflineException(PropertySubscriber subscriber, Exception exception) {
        System.err.println("[" + name + "] Subscriber [" + subscriber.getPropertyKey() + "] " +
                "failed to handle offOfficer event: " +
                exception.getMessage());
        exception.printStackTrace(System.err);
    }
}