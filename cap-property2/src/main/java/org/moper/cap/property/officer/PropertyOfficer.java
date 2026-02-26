package org.moper.cap.property.officer;

import org.moper.cap.core.context.ResourceContext;
import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 属性管理平台 </br>
 *
 * 负责管理属性发布者，提供获取、添加、删除等功能。 </br>
 */
public interface PropertyOfficer extends PropertyView, ResourceContext {

    void receive(PropertyManifest manifest);

    void receiveAsync(PropertyManifest manifest);

    PropertyPublisher getPublisher(String name);

    PropertyPublisher getPublisher(String name, Supplier<PropertyPublisher> supplier);

    boolean containsPublisher(String name);

    void destroyPublisher(String name) throws Exception;

    Collection<PropertyPublisher> getAllPublishers();

    PropertySubscription createSubscription(Supplier<PropertySubscription> supplier);

    boolean containsSubscription(PropertySubscription subscription);

    void destroySubscription(PropertySubscription subscription) throws Exception;

    Collection<PropertySubscription> getAllSubscriptions();
}
