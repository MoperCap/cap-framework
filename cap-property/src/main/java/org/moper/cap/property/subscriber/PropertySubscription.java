package org.moper.cap.property.subscriber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.officer.PropertyOfficer;

/**
 * 属性订阅客户端接口 </br>
 *
 * 属性订阅客户端用于将多个属性订阅者进行分组管理，方便统一处理相关事件和资源。 </br>
 * 属性订阅客户端可以包含多个属性订阅者，每个订阅者负责订阅不同的属性。 </br>
 * 属性订阅客户端的生命周期由用户自行管理，用户可以根据需要创建和销毁属性订阅客户端。 </br>
 *
 * 设计特性：
 * <ul>
 *   <li><strong>订阅者静态管理</strong>：订阅者在初始化时确定，创建后不支持动态修改</li>
 *   <li><strong>选择器不可变</strong>：属性选择器在初始化时确定，创建后保持不变</li>
 *   <li><strong>被动事件接收</strong>：订阅客户端通过 dispatch() 方法被动接收属性更新事件</li>
 *   <li><strong>事件分发</strong>：根据订阅者的兴趣范围，将属性操作分发给相应的订阅者</li>
 *   <li><strong>简化设计</strong>：避免动态修改导致的 Officer 缓存不一致问题</li>
 * </ul>
 *
 * 设计说明：
 * 订阅者客户端在创建时即确定所有订阅者和属性选择器。创建后，不支持：
 * <ul>
 *   <li>添加新的订阅者</li>
 *   <li>移除订阅者</li>
 *   <li>修改属性选择器</li>
 * </ul>
 *
 * 这种设计的优势：
 * <ul>
 *   <li>避免了 Officer 缓存与 Subscription 状态不一致的问题</li>
 *   <li>简化了系统设计，易于理解和维护</li>
 *   <li>避免了初始化后无法进行精确的属性通知的问题</li>
 *   <li>新增订阅者时，可以创建新的 PropertySubscription 实例重新注册到 Officer</li>
 * </ul>
 *
 * 使用示例：
 * <pre>
 * // 创建订阅者列表
 * List&lt;PropertySubscriber&gt; subscribers = new ArrayList&lt;&gt;();
 * subscribers.add(new MyPropertySubscriber("db.host"));
 * subscribers.add(new MyPropertySubscriber("db.port"));
 *
 * // 创建订阅客户端，订阅者在初始化时确定
 * PropertySubscription subscription = DefaultPropertySubscription.builder()
 *     .name("DatabaseConfigSubscription")
 *     .selector(key -> key.startsWith("db."))
 *     .subscribers(subscribers)
 *     .build();
 *
 * // 将订阅客户端注册到 Officer
 * officer.subsribe(subscription);
 *
 * // 之后 Officer 会向该订阅客户端分发相关的属性更新事件
 *
 * // 如果需要修改订阅者，创建新的订阅客户端
 * List&lt;PropertySubscriber&gt; newSubscribers = new ArrayList&lt;&gt;();
 * newSubscribers.add(new MyPropertySubscriber("db.pool.size"));
 * PropertySubscription newSubscription = DefaultPropertySubscription.builder()
 *     .name("DatabasePoolSubscription")
 *     .selector(key -> key.startsWith("db.pool"))
 *     .subscribers(newSubscribers)
 *     .build();
 * officer.subsribe(newSubscription);
 * </pre>
 *
 * @author MoperCap
 * @since 1.0
 */
public interface PropertySubscription {

    /**
     * 获取当前属性订阅客户端的名称。</br>
     *
     * 名称是对该订阅客户端的描述，通常用于日志输出和监控。
     * 名称用于标识该订阅客户端，使调试和问题追踪更加便利。
     *
     * @return 当前属性订阅客户端的名称
     */
    @NotBlank String name();

    /**
     * 获取当前属性订阅客户端的属性选择器。</br>
     *
     * 属性选择器用于确定该订阅客户端关心的属性范围。Officer 会根据这个选择器
     * 来决定是否将属性操作分发给这个订阅客户端。</br>
     *
     * 该属性选择器在创建时确定，之后保持不变。</br>
     *
     * 选择器的匹配结果决定了该订阅客户端是否会接收相关的属性更新事件。</br>
     *
     * @return 当前属性订阅客户端的属性选择器（不可变）
     */
    @NotNull PropertySelector selector();

    /**
     * 接收属性管理平台发送的事件清单。</br>
     *
     * 当属性管理平台接收到新的事件清单并更新状态后，将通知所有相关的订阅者进行相应的处理。
     * 该方法将接收到的属性操作分发给该订阅客户端中的所有订阅者。</br>
     *
     * 由于属性订阅客户端不参与属性相关决策，因此不需要对接收到的事件清单进行校验，
     * 直接将事件清单分发给订阅者进行处理即可。</br>
     *
     * 分发策略：
     * <ul>
     *   <li>遍历所有订阅者</li>
     *   <li>检查该订阅者是否关心该属性</li>
     *   <li>若关心，则调用相应的 onSet() 或 onRemoved() 方法</li>
     * </ul>
     *
     * 如果订阅客户端已关闭，该方法将忽略接收的事件。</br>
     *
     * @param operations 属性管理平台发送的事件清单
     */
    void dispatch(@NotEmpty PropertyOperation... operations);

    /**
     * 当属性发布者不在线时，属性管理平台将接收到通知，并进行相应的处理。</br>
     *
     * 该方法允许订阅客户端在属性发布者离线时执行清理工作。</br>
     * 默认实现可以不进行任何操作，子类可以覆盖该方法以实现自定义的离线处理逻辑。</br>
     *
     * 例如：清理与该 Officer 相关的资源、重新订阅其他 Officer 等。</br>
     *
     * @param officer 不在线的属性官管理平台
     */
    void offOfficer(@NotNull PropertyOfficer officer);

    /**
     * 判断当前订阅客户端是否已关闭。</br>
     *
     * 如果订阅客户端已关闭，则该客户端将不再接收新的事件。
     *
     * @return 如果订阅客户端已关闭则返回 true，否则返回 false
     */
    boolean isClosed();

    /**
     * 关闭属性订阅客户端。</br>
     *
     * 关闭后，该订阅客户端将不再接收新的事件。</br>
     *
     * 具体行为说明：
     * <ul>
     *   <li>已关闭的订阅客户端将忽略 dispatch() 方法发送的事件</li>
     *   <li>close() 方法可以被重复调用，是幂等的</li>
     * </ul>
     *
     * 注意：订阅者在创建时确定，关闭后也不会被清空。
     */
    void close();
}