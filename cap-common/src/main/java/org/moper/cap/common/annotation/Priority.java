package org.moper.cap.common.annotation;

/**
 * 优先级注解 </br>
 *
 * 用于框架内部多个 SPI 工具之间的排序 </p>
 * 具体处理逻辑由各个工具自行负责 </p>
 */
public @interface Priority {

    int value() default 0;
}
