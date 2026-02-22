package org.moper.cap.bean.container;

import jakarta.validation.constraints.NotNull;
import org.moper.cap.bean.exception.BeanException;
import org.moper.cap.bean.interceptor.BeanInterceptor;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Bean容器接口
 *
 * <p><b>核心语义：IoC容器的完整能力集合</b>
 *
 * <p>此接口等价于：
 * <pre>
 * BeanProvider + BeanInspector + BeanRegistry + 容器生命周期管理
 * </pre>
 *
 * <p><b>核心流程：</b>
 * <pre>
 * 注册 → 创建 → 注入 → 生命周期回调 → 获取
 */
public interface BeanContainer extends BeanProvider, BeanInspector, BeanRegistry {

    /**
     * 添加Bean拦截器
     *
     * @param interceptor Bean拦截器，不能为null
     * @throws BeanException 如果配置已冻结
     */
    void addBeanInterceptor(@NotNull BeanInterceptor interceptor) throws BeanException;

    /**
     * 预实例化所有非懒加载的单例Bean
     *
     * @throws BeanException 如果实例化失败
     */
    void preInstantiateSingletons() throws BeanException;

    /**
     * 冻结配置
     */
    void freezeConfiguration();

    /**
     * 检查配置是否已冻结
     *
     * @return 如果已冻结返回true，否则返回false
     */
    boolean isConfigurationFrozen();

    /**
     * 销毁所有单例Bean
     *
     * @throws BeanException 如果销毁失败
     */
    void destroySingletons() throws BeanException;

    /**
     * 根据类型获取所有匹配的Bean实例
     *
     * @param type 目标类型
     * @param <T> 类型参数
     * @return Bean名称到实例的映射，如果没有则返回空Map
     * @throws BeanException 如果Bean实例化失败
     */
    <T> @NotNull Map<String, T> getBeansOfType(@NotNull Class<T> type) throws BeanException;

    /**
     * 根据注解类型获取所有标注了该注解的Bean实例
     *
     * @param annotationType 注解类型
     * @return Bean名称到实例的映射，如果没有则返回空Map
     * @throws BeanException 如果Bean实例化失败
     */
    @NotNull Map<String, Object> getBeansWithAnnotation(@NotNull Class<? extends Annotation> annotationType) throws BeanException;

    /**
     * 获取Bean创建器（内部使用）
     *
     * @return Bean创建器
     */
    @NotNull BeanCreator getBeanCreator();

    /**
     * 获取Bean处理器（内部使用）
     *
     * @return Bean处理器
     */
    @NotNull BeanProcessor getBeanProcessor();
}