package org.moper.cap.property.subscriber;

import org.moper.cap.core.resource.CloseableResource;

/**
 * 属性订阅客户端 </br>
 *
 * 负责管理一个或多个属性订阅者，并向属性管理平台对接。充当属性管理平台与属性订阅者之间的中介 </br>
 * 属性订阅客户端需要负责管理其内部的所有属性订阅者的生命周期。
 */
public interface PropertySubscription extends CloseableResource, Iterable<PropertySubscriber<?>> {
}
