package org.moper.cap.property.publisher;

import org.moper.cap.core.context.DispatcherContext;
import org.moper.cap.property.event.PropertyOperation;
import org.moper.cap.property.event.PublisherManifest;
import org.moper.cap.property.exception.PropertyException;
import org.moper.cap.property.exception.PropertyManifestVersionException;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.result.PublisherManifestResult;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


/**
 * 属性发布者接口 </br>
 * 属性发布者负责发布属性相关事件，并与属性管理平台进行签约和解约 </br>
 * 属性发布者只提供属性更新事件的发布功能，不直接处理属性查询和订阅等功能 </br>
 */
public interface PropertyPublisher extends DispatcherContext {


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
    int currentVersion();

    /**
     * 以同步的方式向所有已签约的Officer发布事件清单 </br>
     *
     * @param operations 发布的事件列表
     * @return 每个已签约的Officer针对该事件列表的处理结果
     */
    List<PublisherManifestResult> publish(PropertyOperation... operations);

    /**
     * 以异步的方式向所有已签约的Officer发布事件清单 </br>
     *
     * @param operations 发布的事件列表
     * @return 每个已签约的Officer针对该事件列表的处理结果
     */
    List<CompletableFuture<PublisherManifestResult>> publishAsync(PropertyOperation... operations);

    /**
     * 拉取指定版本号的事件清单 </br>
     *
     * @param versionID 版本号
     * @return 指定版本号的事件清单
     * @throws PropertyManifestVersionException 若版本号无效，则抛出异常
     */
    PublisherManifest pull(int versionID) throws PropertyManifestVersionException;

    /**
     * 拉取指定版本范围 [beginVersionID, endVersionID) 内的事件清单 </br>
     *
     * @param beginVersionID 起始版本号（包含）
     * @param endVersionID 结束版本号（不包含）
     * @return 指定版本范围的事件清单列表
     * @throws PropertyManifestVersionException 若版本号无效，则抛出异常
     */
    List<PublisherManifest> pull(int beginVersionID, int endVersionID) throws PropertyManifestVersionException;

    /**
     * 与Officer签约，允许该Officer接收本Publisher发布的事件
     *
     * @param officer 签约的Officer
     * @exception PropertyException 若签约过程中发生错误，例如发布者已关闭，则抛出异常
     */
    void contract(PropertyOfficer officer) throws PropertyException;

    /**
     * 与Officer解约，禁止该Officer接收本Publisher发布的事件 </br>
     * 解约后，Publisher保证不再向该Officer发布事件，但不保证该Officer已接收的事件不会被处理
     *
     * @param officer 解约的Officer
     */
    void uncontract(PropertyOfficer officer);

    /**
     * 获取当前已签约的Officer数量
     *
     * @return 当前已签约的Officer数量
     */
    int getOfficerCount();

    /**
     * 获取当前已签约的Officer副本集合
     *
     * @return 当前已签约的Officer副本集合
     */
    Set<PropertyOfficer> getOfficers();

    /**
     * 判断Publisher是否已与指定Officer签约
     *
     * @param officer 指定Officer
     * @return 若已签约，则返回true；否则返回false
     */
    boolean isContractOfficer(PropertyOfficer officer);

    /**
     * 判断发布者是否已关闭 </br>
     *
     * @return 若发布者已关闭，则返回true；否则返回false
     */
    boolean isClosed();

    /**
     * 关闭发布者，禁止发布者继续发布事件，并通知所有的Officer </br>
     */
    void close();
}
