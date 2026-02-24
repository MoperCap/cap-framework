package org.moper.cap.property.officer;

import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * 属性管理平台 </br>
 *
 * 负责管理属性发布者，提供获取、添加、删除等功能。 </br>
 */
public interface PropertyOfficer extends PropertyView{

    /**
     * 获取指定名称的属性发布者，若不存在则自动构造默认的属性发布者并返回。 </br>
     *
     * @param name 属性发布者的名称，不能为空或空白字符串
     * @return 指定名称的属性发布者，如果不存在则返回一个默认属性发布者
     */
    PropertyPublisher getPublisher(String name);

    /**
     * 获取指定名称的属性发布者，若不存在则使用提供的工厂函数创建一个新的属性发布者并返回。 </br>
     * 工厂函数接受当前的属性管理平台实例和属性发布者的名称作为参数，返回一个新的属性发布者实例。 </br>
     *
     * @param name 属性发布者的名称，不能为空或空白字符串
     * @param factory 用于创建属性发布者的工厂函数，不能为空
     * @return 指定名称的属性发布者，如果不存在则返回工厂函数创建的新属性发布者
     */
    PropertyPublisher getPublisher(String name, BiFunction<PropertyOfficer, String, PropertyPublisher> factory);

    /**
     * 检查是否存在指定名称的属性发布者。 </br>
     *
     * @param name 属性发布者的名称，不能为空或空白字符串
     * @return 如果存在指定名称的属性发布者则返回true，否则返回false
     */
    boolean containsPublisher(String name);

    /**
     * 移除指定名称的属性发布者并返回被移除的属性发布者实例，如果不存在则返回null。 </br>
     *
     * @param name 属性发布者的名称，不能为空或空白字符串
     * @return 被移除的属性发布者实例，如果不存在则返回null
     */
    PropertyPublisher removePublisher(String name);

    /**
     * 获取所有已注册的属性发布者的集合。 </br>
     *
     * @return 所有已注册的属性发布者的集合，如果没有任何发布者则返回一个空集合
     */
    Collection<? extends PropertyPublisher> getAllPublishers();

    /**
     * 以同步的方式接收属性发布者发布的事件清单，并根据事件清单更新内部状态。 </br>
     *
     *
     * @param manifest 事件清单，不能为空
     * @throws org.moper.cap.property.exception.PropertyException 如果事件清单无效或处理过程中发生错误
     */
    void receive(PropertyManifest manifest);

    /**
     * 以异步的方式接收属性发布者发布的事件清单，并根据事件清单更新内部状态。 </br>
     *
     * @param manifest 事件清单，不能为空
     * @throws org.moper.cap.property.exception.PropertyException 如果事件清单无效或处理过程中发生错误
     */
    void receiveAsync(PropertyManifest manifest);

    void subscribe(PropertySubscription subscription);

    void unsubscribe(PropertySubscription subscription);
}
