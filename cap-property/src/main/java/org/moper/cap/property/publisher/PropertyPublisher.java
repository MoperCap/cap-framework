package org.moper.cap.property.publisher;

import org.moper.cap.property.event.PropertyManifest;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.officer.PropertyOfficer;


/**
 * 属性发布者接口 </br>
 * 属性发布者负责发布属性相关事件，并与属性管理平台进行签约和解约 </br>
 * 属性发布者只提供属性更新事件的发布功能，不直接处理属性查询和订阅等功能 </br>
 */
public interface PropertyPublisher {


    /**
     * 获取当前发布者的名称
     *
     * @return 当前发布者的名称
     */
    String name();

    /**
     * 获取当前发布者的版本号
     *
     * @return 当前发布者的版本号
     */
    long currentVersion();

    /**
     * 向所有已签约的Officer发布事件
     *
     * @param events 事件列表
     * @return 是否成功发布
     */
    boolean publish(PropertyOperation... events);

    /**
     * 向所有已签约的Officer发布事件
     *
     * @param manifest 事件清单
     * @return 是否成功发布
     */
    boolean publish(PropertyManifest manifest);

    /**
     * 拉取指定版本范围 [beginVersionID, endVersionID) 内的事件清单 </br>
     * Publisher可根据自身情况选择是否支持对多PublisherManifest清单的自动合并 </br>
     *
     * @param beginVersionID 起始版本号（包含）
     * @param endVersionID 结束版本号（不包含）
     * @return 事件清单
     * @throws Exception 如果拉取过程中发生错误或者版本范围无效，则抛出异常
     */
    PropertyManifest pull(long beginVersionID, long endVersionID) throws Exception;

    /**
     * 与Officer签约，允许该Officer接收本Publisher发布的事件
     *
     * @param officer 签约的Officer
     */
    void contract(PropertyOfficer officer) throws Exception;

    /**
     * 与Officer解约，禁止该Officer接收本Publisher发布的事件 </br>
     * 解约后，Publisher保证不再向该Officer发布事件，但不保证该Officer已接收的事件不会被处理
     *
     * @param officer 解约的Officer
     */
    void uncontract(PropertyOfficer officer);
}
