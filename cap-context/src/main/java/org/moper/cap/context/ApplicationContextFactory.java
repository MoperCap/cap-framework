package org.moper.cap.context;

import org.moper.cap.core.exception.CapFrameworkException;

/**
 * ApplicationContext工厂接口 </br>
 * 用于从BootstrapContext构建ApplicationContext
 *
 * @param <T> ApplicationContext的具体类型
 */
@FunctionalInterface
public interface ApplicationContextFactory<T extends ApplicationContext> {

    /**
     * 从BootstrapContext构建ApplicationContext
     *
     * @param context 初始化阶段系统综合上下文
     * @return  运行阶段系统应用上下文
     * @throws CapFrameworkException 若构建失败，则抛出异常
     */
    T create(BootstrapContext context) throws CapFrameworkException;
}
