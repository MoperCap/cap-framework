package org.moper.cap.property.annotation;

import java.lang.annotation.*;


/**
 * 属性订阅者客户端注解 </br>
 *
 * 标记该类为一个属性订阅者客户端
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscription {
    String value() default "";
}
