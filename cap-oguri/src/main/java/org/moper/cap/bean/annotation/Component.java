package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标注一个类为框架管理的组件，使其在组件扫描时被自动发现并注册为 Bean
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
}
