package org.moper.cap.property.publisher;

import org.moper.cap.core.resource.CloseableResource;
import org.moper.cap.property.event.PropertyOperation;

/**
 * 属性发布者接口 </br>
 *
 * 不提供属性获取功能，仅对外提供向属性管理平台发布属性操作的能力。 </br>
 */
public interface PropertyPublisher extends CloseableResource {
    /**
     * 发布属性操作列表
     *
     * @param operations 属性操作列表
     */
    void publish(PropertyOperation... operations);

    /**
     * 异步发布属性操作列表
     *
     * @param operations 属性操作列表
     */
    void publishAsync(PropertyOperation... operations);
}
