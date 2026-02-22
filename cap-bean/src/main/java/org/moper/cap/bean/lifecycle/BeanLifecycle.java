package org.moper.cap.bean.lifecycle;

import org.moper.cap.bean.exception.BeanLifecycleException;

/**
 * Bean 生命周期回调接口
 *
 * <p>需要自定义初始化或销毁逻辑的 Bean 可以实现此接口。
 * 两个方法均有空的默认实现，只需重写关心的阶段。
 *
 * <p><b>为什么是一个接口而不是两个？</b>
 * Spring 将 {@code InitializingBean} 和 {@code DisposableBean} 设计为两个独立接口，
 * 是因为 Java 8 之前的接口不支持 {@code default} 方法，合并会强迫用户实现不需要的方法。
 * Java 17 中 {@code default} 方法完全解决了这个问题，合并为一个接口更简洁。
 *
 * <p><b>调用时机：</b>
 * <pre>
 * 初始化阶段（在 BeanCreator.initializeBean 内部）：
 *   applyBeforeInitialization（拦截器链）
 *     → BeanLifecycle.afterPropertiesSet()   ← 此接口
 *   applyAfterInitialization（拦截器链）
 *
 * 销毁阶段（在 BeanContainer.destroySingletons 内部）：
 *   → BeanLifecycle.destroy()                ← 此接口
 * </pre>
 *
 * <p><b>与拦截器的职责区分：</b>
 * <ul>
 *   <li>{@link org.moper.cap.bean.interceptor.BeanInterceptor} 面向框架扩展者，
 *       处理横切关注点（如 AOP 代理创建、注解处理）</li>
 *   <li>{@code BeanLifecycle} 面向普通用户，处理 Bean 自身的业务初始化/资源释放</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // 只需要初始化
 * public class ConnectionPool implements BeanLifecycle {
 *     @Override
 *     public void afterPropertiesSet() {
 *         this.connections = buildConnections();
 *     }
 * }
 *
 * // 只需要销毁
 * public class FileWatcher implements BeanLifecycle {
 *     @Override
 *     public void destroy() {
 *         this.watcher.close();
 *     }
 * }
 *
 * // 两者都需要
 * public class CacheManager implements BeanLifecycle {
 *     @Override
 *     public void afterPropertiesSet() { this.cache = buildCache(); }
 *
 *     @Override
 *     public void destroy() { this.cache.invalidateAll(); }
 * }
 * }</pre>
 */
public interface BeanLifecycle {

    /**
     * 在所有属性注入完成后由容器调用，执行初始化逻辑
     *
     * <p>此时所有通过构造函数注入的依赖均已就绪，可以安全访问。
     *
     * <p><b>典型用途：</b>
     * <ul>
     *   <li>建立数据库连接、网络连接等资源</li>
     *   <li>预热缓存、加载配置</li>
     *   <li>启动后台线程</li>
     *   <li>校验注入的依赖是否符合预期</li>
     * </ul>
     *
     * @throws BeanLifecycleException 初始化失败时抛出，容器将其包装为
     *                   {@link org.moper.cap.bean.exception.BeanInitializationException}
     */
    default void afterPropertiesSet() throws BeanLifecycleException {
        // 默认无操作，子类按需重写
    }

    /**
     * 在容器关闭时由容器调用，执行资源释放逻辑
     *
     * <p><b>注意：此方法只对单例 Bean 有效。</b>
     * 原型 Bean 的生命周期由调用方负责，容器不追踪原型 Bean 的销毁。
     *
     * <p><b>典型用途：</b>
     * <ul>
     *   <li>关闭数据库连接、网络连接</li>
     *   <li>停止后台线程、定时任务</li>
     *   <li>释放文件句柄、清理临时资源</li>
     * </ul>
     *
     * <p><b>实现建议：</b>此方法应当幂等，即多次调用与调用一次效果相同。
     *
     * @throws Exception 销毁失败时抛出，容器将其包装为
     *                   {@link org.moper.cap.bean.exception.BeanDestructionException}
     */
    default void destroy() throws BeanLifecycleException {
        // 默认无操作，子类按需重写
    }
}