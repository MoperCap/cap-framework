package org.moper.cap.boot.annotation;

import java.lang.annotation.*;

/**
 * 标注懒加载 Bean，首次 getBean 时才创建实例
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lazy {
}
