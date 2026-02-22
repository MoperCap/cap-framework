package org.moper.cap.bean.lifecycle;

import org.moper.cap.bean.exception.BeanException;

/**
 * Bean销毁回调接口
 *
 * <p><b>核心语义：Bean 在容器关闭时，可自行释放持有的资源</b>
 *
 * <p>与 Spring 的 {@code DisposableBean} 对应，但方法命名更直观：
 * <ul>
 *   <li>Spring: {@code destroy()} —— 与本接口同名，但此处语义更明确</li>
 *   <li>Cap:    {@code destroy()} —— 保持一致，无歧义</li>
 * </ul>
 *
 * <p><b>调用时机：</b>在容器调用 {@code BeanContainer.destroySingletons()} 时，
 * 由 {@link org.moper.cap.bean.container.BeanProcessor#invokeDestroyCallbacks} 触发。
 *
 * <p><b>调用顺序（在 destroySingletons 内部）：</b>
 * <pre>
 * BeanDisposable.destroy()      ← 此接口
 *   → 自定义 destroyMethodName（反射）
 * </pre>
 *
 * <p><b>设计原则（参考 Guice）：</b>
 * <ul>
 *   <li>不注入任何容器引用（无 Aware）</li>
 *   <li>只做资源释放，不访问其他 Bean</li>
 *   <li>实现应当幂等（多次调用安全）</li>
 * </ul>
 */
public interface BeanDisposable {

    /**
     * 执行Bean的销毁逻辑，释放持有的资源
     *
     * <p>此方法在容器关闭时调用，可以在此处：
     * <ul>
     *   <li>关闭数据库连接、网络连接等</li>
     *   <li>停止后台线程、定时任务等</li>
     *   <li>释放文件句柄、清理临时资源等</li>
     * </ul>
     *
     * <p><b>实现建议：</b>此方法应当幂等，即多次调用与调用一次效果相同。
     *
     * @throws BeanException 如果销毁失败
     */
    void destroy() throws BeanException;
}