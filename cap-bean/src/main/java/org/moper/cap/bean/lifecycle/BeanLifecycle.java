package org.moper.cap.bean.lifecycle;

/**
 * Bean 生命周期回调接口。
 *
 * <p>需要自定义初始化或销毁逻辑的 Bean 可实现此接口。
 * 两个方法均有空行为的默认实现，只需重写关心的阶段即可。
 *
 * <p><b>异常处理约定：</b>方法签名声明 {@code throws Exception}，
 * 允许用户直接抛出任何异常，无需手动包装。框架在调用点统一捕获并转换：
 * <ul>
 *   <li>{@code afterPropertiesSet()} 的异常 →
 *       {@link org.moper.cap.bean.exception.BeanInitializationException}</li>
 *   <li>{@code destroy()} 的异常 →
 *       {@link org.moper.cap.bean.exception.BeanDestructionException}</li>
 * </ul>
 *
 * <p><b>调用时机：</b>
 * <pre>
 * 初始化阶段（BeanCreator.initializeBean 内部）：
 *   BeanProcessor.applyBeforeInitialization（拦截器链）
 *     → afterPropertiesSet()   ← 此处
 *   BeanProcessor.applyAfterInitialization（拦截器链）
 *
 * 销毁阶段（BeanContainer.destroySingletons 内部，仅单例）：
 *     → destroy()              ← 此处
 * </pre>
 */
public interface BeanLifecycle {

    /**
     * 在所有依赖注入完成后由容器调用，执行初始化逻辑。
     *
     * @throws Exception 初始化过程中发生的任何异常，框架负责统一包装
     */
    default void afterPropertiesSet() throws Exception {
    }

    /**
     * 在容器关闭时由容器调用，执行资源释放逻辑。
     *
     * <p><b>注意：此方法只对单例 Bean 有效。</b>
     * <p><b>实现建议：</b>此方法应当幂等，多次调用与调用一次效果相同。
     *
     * @throws Exception 销毁过程中发生的任何异常，框架负责统一包装
     */
    default void destroy() throws Exception {
    }
}