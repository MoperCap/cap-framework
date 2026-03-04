package org.moper.cap.bean.exception;

/**
 * 注册、移除 BeanDefinition 或 BeanDefinition 校验失败时抛出。
 *
 * <p><b>典型场景：</b>
 * <ul>
 *   <li>Bean 名称已存在且不允许覆盖</li>
 *   <li>容器配置已冻结后仍尝试注册或移除</li>
 *   <li>BeanDefinition 字段组合非法</li>
 * </ul>
 */
public class BeanDefinitionStoreException extends BeanDefinitionException {

    public BeanDefinitionStoreException(String message) {
        super(message);
    }

    public BeanDefinitionStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}