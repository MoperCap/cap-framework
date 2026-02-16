package org.moper.cap.property.subscriber;

import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.officer.PropertyOfficer;

/**
 * 属性订阅客户端接口 </br>
 *
 * 属性订阅客户端用于将多个属性订阅者进行分组管理，方便统一处理相关事件和资源。 </br>
 * 属性订阅客户端可以包含多个属性订阅者，每个订阅者负责订阅不同的属性。 </br>
 * 属性订阅客户端的生命周期由用户自行管理，用户可以根据需要创建和销毁属性订阅客户端
 */
public interface PropertySubscription{

    /**
     * 获取当前属性订阅客户端的属性选择器 </br>
     *
     * @return 当前属性订阅客户端的属性选择器
     */
    PropertySelector selector();

    /**
     * 接收属性管理平台发送的事件清单 </br>
     * 当属性管理平台接收到新的事件清单并更新状态后，将通知所有相关的订阅者进行相应的处理 </br>
     *
     * @param manifest 事件清单
     */
    void dispatch(PropertyManifest manifest);

    /**
     * 当属性发布者被销毁时，属性管理平台将接收到通知，并进行相应的处理 </br>
     *
     *
     * @param officer 被销毁的属性官管理平台
     */
    void onOfficerDestroyed(PropertyOfficer officer);
}
