package org.moper.cap.bean.annotation;

import java.lang.annotation.*;

/**
 * 标注主候选 Bean，按类型查找时若存在多个匹配则优先返回此 Bean
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Primary {
}
