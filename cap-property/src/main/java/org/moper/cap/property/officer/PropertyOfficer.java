package org.moper.cap.property.officer;

import org.moper.cap.property.util.PropertyLifecycle;
import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 属性管理平台 </br>
 *
 * 负责管理属性发布者与属性订阅者客户端，提供获取（创建）、删除等功能。 </br>
 */
public interface PropertyOfficer extends PropertyView, PropertyLifecycle {

    /**
     * 以同步的方式接收来自属性发布者发送的属性操作清单
     *
     * @param manifest 属性操作清单，不可为null
     */
    void receive(PropertyManifest manifest);

    /**
     * 以异步的方式接收来自属性发布者发送的属性操作清单
     *
     * @param manifest 属性操作清单，不可为null
     */
    void receiveAsync(PropertyManifest manifest);

    /**
     * 获取指定名的属性发布者 </br>
     *
     * @param name 属性发布者名称，不可为null或blank
     * @return 若存在指定的属性发布者则返回；否则创建默认的属性发布者并返回
     */
    PropertyPublisher getPublisher(String name);

    /**
     * 获取指定名的属性发布者 </br>
     *
     * @param name 属性发布者名称，不可为null或blank
     * @param supplier 属性发布者获取方法，不能为null，且返回的属性发布者也不能为null
     * @return 若存在指定的属性发布者则返回；否则根据指定的属性发布者获取方法创建并返回
     */
    PropertyPublisher getPublisher(String name, Supplier<PropertyPublisher> supplier);

    /**
     * 检查是否存在指定名的属性发布者
     *
     * @param name 属性发布者名称，不可为null或blank
     * @return 若存在则返回true；否则返回false
     */
    boolean containsPublisher(String name);

    /**
     * 销毁指定名的属性发布者 </br>
     *
     * @param name 属性发布者名称，不可为null或blank
     */
    void destroyPublisher(String name);

    /**
     * 获取所有的属性发布者的集合
     *
     * @return 属性发布者集合。若不存在任何属性发布者，则返回一个空集合
     */
    Collection<PropertyPublisher> getAllPublishers();

    /**
     * 根据指定的属性订阅者客户端
     *
     * @param name 属性订阅者客户端名称，不能为null或blank
     * @return 若存在指定的属性订阅者客户端则返回；否则返回null
     */
    PropertySubscription getSubscription(String name);

    /**
     * 根据指定的属性订阅者客户端
     *
     * @param name 属性订阅者客户端名称，不能为null或blank
     * @param supplier 属性订阅者客户端生成方法，不能为null，且返回的属性订阅客户端也不能为null
     * @return 若存在指定的属性订阅者客户端则返回；否则根据指定的属性订阅者客户端生成方法生成相应的客户端并返回
     */
    PropertySubscription getSubscription(String name, Supplier<PropertySubscription> supplier);

    /**
     * 检查是否存在指定名的属性订阅客户端
     *
     * @param name 属性订阅客户端，不能为null
     * @return 若存在则返回true；否则返回false
     */
    boolean containsSubscription(String name);

    /**
     * 销毁指定的属性订阅客户端
     *
     * @param name 属性订阅客户端，不能为null
     */
    void destroySubscription(String name);

    /**
     * 获取所有的属性订阅客户端的集合
     *
     * @return 属性订阅客户端集合。若不存在属性订阅客户端，则返回一个空集合
     */
    Collection<PropertySubscription> getAllSubscriptions();
}
