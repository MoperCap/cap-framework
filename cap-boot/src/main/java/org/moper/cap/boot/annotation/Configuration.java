package org.moper.cap.boot.annotation;

import java.lang.annotation.*;

/**
 * 标注一个类为配置类，包含 Bean 定义或框架配置
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
}
