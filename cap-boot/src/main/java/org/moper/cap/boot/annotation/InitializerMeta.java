package org.moper.cap.boot.annotation;


import org.moper.cap.boot.bootstrap.InitializerType;

import java.lang.annotation.*;

/**
 * 框架启动阶段构造机元信息
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitializerMeta {

    /**
     * 构造机类型
     */
    InitializerType type() default InitializerType.EXTENSION;

    /**
     * 同级别构造机优先级
     */
    int order();

    /**
     * 构造机名
     */
    String name() default "";

    /**
     * 构造机描述
     */
    String description() default "";
}
