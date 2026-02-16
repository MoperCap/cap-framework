package org.moper.cap.property.officer;

import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.subscriber.PropertySubscription;

import java.util.Set;

/**
 * 属性官管理平台接口 </br>
 * 属性官管理平台负责接收属性发布者发布的事件清单，并根据事件清单更新内部状态 </br>
 * 属性官管理平台还负责通知相关的订阅者和视图池进行相应的更新 </br>
 */
public interface PropertyOfficer {

    /**
     * 获取当前属性管理平台的名称
     *
     * @return 当前属性管理平台的名称
     */
    String name();

    /**
     * 获取当前属性管理平台的版本号
     *
     * @return 当前属性管理平台的版本号
     */
    long currentVersion();

    /**
     * 接收Publisher发布的事件清单 </br>
     * 属性管理平台将根据事件清单更新内部状态，并通知相关Subscriber和ViewPool进行相应的更新 </br>
     *
     * @param manifest 事件清单
     * @return 是否成功接收事件清单
     */
    boolean receive(PropertyManifest manifest);

    /**
     * 当属性发布者被销毁时，属性管理平台将接收到通知，并进行相应的处理 </br>
     * 处理可能包括清理相关的状态、通知相关的订阅者和视图池等 </br>
     * 注意：属性发布者被销毁后，属性管理平台将不再接收该发布者的事件清单 </br>
     *
     *
     * @param publisher 被销毁的属性发布者
     */
    void onPublisherDestroyed(PropertyPublisher publisher);

    /**
     * 订阅属性更新事件 </br>
     * 当属性管理平台接收到新的事件清单并更新状态后，将通知所有相关的订阅者进行相应的处理 </br>
     *
     * @param subscription 订阅者客户端
     */
    void subsribe(PropertySubscription subscription);

    /**
     * 取消订阅属性更新事件 </br>
     * 取消订阅后，属性管理平台将不再通知该订阅者相关属性的更新事件 </br>
     *
     * @param subscription 取消订阅的订阅者客户端
     */
    void unsubscribe(PropertySubscription subscription);
}
