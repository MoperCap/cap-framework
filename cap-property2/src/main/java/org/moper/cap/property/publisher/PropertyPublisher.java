package org.moper.cap.property.publisher;

import org.moper.cap.core.context.ResourceContext;
import org.moper.cap.property.event.PropertyOperation;

/**
 * 属性发布者接口
 */
public interface PropertyPublisher extends ResourceContext {

    /**
     * 获取发布者名称
     *
     * @return 发布者名称
     */
    String name();

    /**
     * 发布属性操作
     *
     * @param operations 属性操作列表
     */
    void publish(PropertyOperation... operations);

    /**
     * 异步发布属性操作
     *
     * @param operations 属性操作列表
     */
    void publishAsync(PropertyOperation... operations);
}
