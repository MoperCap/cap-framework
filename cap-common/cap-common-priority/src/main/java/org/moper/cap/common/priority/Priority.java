package org.moper.cap.common.priority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 优先级注解 </br>
 *
 * 用于框架内部多个 SPI 工具之间的排序 </p>
 * 具体处理逻辑由各个工具自行负责 </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Priority {
    int value() default 0;
}
