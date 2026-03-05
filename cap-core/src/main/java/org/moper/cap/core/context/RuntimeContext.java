package org.moper.cap.core.context;

import org.moper.cap.bean.container.BeanInspector;
import org.moper.cap.bean.container.BeanProvider;
import org.moper.cap.property.officer.PropertyOfficer;

/**
 * 框架运行期系统上下文。
 *
 * 典型用法：
 * try (ApplicationContext context =
 *         new DefaultBootstrapContext(AppConfig.class, args)
 *             .build(DefaultApplicationContextFactory.INSTANCE)) {
 *     context.run();
 * }
 */
public interface RuntimeContext extends BeanProvider, BeanInspector, AutoCloseable {

    /**
     * 获取属性管理平台
     */
    PropertyOfficer getPropertyOfficer();
}