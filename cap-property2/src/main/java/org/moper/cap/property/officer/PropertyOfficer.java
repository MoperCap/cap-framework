package org.moper.cap.property.officer;

import org.moper.cap.core.context.ResourceContext;
import org.moper.cap.property.event.PropertyManifest;

/**
 * 属性管理平台 </br>
 *
 * 负责管理属性发布者，提供获取、添加、删除等功能。 </br>
 */
public interface PropertyOfficer extends PropertyView, PublisherManager, SubscriptionManager, ResourceContext {

    void receive(PropertyManifest manifest);

    void receiveAsync(PropertyManifest manifest);

}
