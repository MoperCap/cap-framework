package org.moper.cap.environment;

import jakarta.validation.constraints.NotNull;
import org.moper.cap.property.officer.PropertyOfficer;
import org.moper.cap.property.publisher.PropertyPublisher;
import org.moper.cap.property.subscriber.PropertyViewPool;

/**
 * 运行期环境上下文。
 * 封装属性系统的访问入口，负责管理所有 PropertyPublisher 的生命周期。
 *
 * 用户若需读取属性，应调用 getViewPool().getPropertyValue(...) 等方法。
 */
public interface Environment extends AutoCloseable {

    /** 获取属性管理平台，供 Initializer 向其发布属性变更事件 */
    @NotNull PropertyOfficer getOfficer();

    /** 获取只读属性视图池，供运行期查询属性值 */
    @NotNull PropertyViewPool getViewPool();

    /**
     * 注册并托管一个 PropertyPublisher 的生命周期。
     * 调用方在 publisher.contract(officer) 之后，应立即调用此方法。
     */
    void registerPublisher(@NotNull PropertyPublisher publisher);

    /**
     * 注销一个已托管的 PropertyPublisher（不自动解约）。
     */
    void unregisterPublisher(@NotNull PropertyPublisher publisher);

    /**
     * 关闭 Environment：
     * 1. 对所有托管 Publisher 调用 uncontract(officer) 然后 close()
     * 2. 关闭 Officer
     */
    @Override
    void close();
}
