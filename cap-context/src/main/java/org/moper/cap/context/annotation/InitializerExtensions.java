package org.moper.cap.context.annotation;

import org.moper.cap.context.bootstrap.Initializer;

import java.lang.annotation.*;

/**
 * 声明额外的框架启动阶段 Initializer构造机 扩展
 * 这些 Initializer构造机 仅在框架启动阶段生效
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitializerExtensions {

    /**
     * 外部 Initializer构造机 实现类列表
     */
    Class<? extends Initializer>[] value();
}
