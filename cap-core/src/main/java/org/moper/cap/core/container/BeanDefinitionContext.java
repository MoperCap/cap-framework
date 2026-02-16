package org.moper.cap.core.container;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.moper.cap.core.context.ResourceContext;

import java.util.Collection;

/**
 * BeanDefinition注册器
 */
public interface BeanDefinitionContext extends ResourceContext {

    /**
     * 注册BeanDefinition
     * @param beanDefinition 待注册的BeanDefinition
     */
    void registerBeanDefinition(@NotNull BeanDefinition beanDefinition);

    boolean containsBeanDefinition(@NotBlank String beanName);

    @NotNull
    BeanDefinition getBeanDefinition(@NotBlank String beanName);

    @NotNull
    Collection<BeanDefinition> getAllBeanDefinition();
}
