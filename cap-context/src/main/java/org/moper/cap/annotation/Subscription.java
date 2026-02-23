package org.moper.cap.annotation;

import java.lang.annotation.*;

/**
 * 属性订阅者客户端注解 </br>
 * 标注在类上，框架会为该类生成一个 PropertySubscription 实例，负责监听属性变更事件并调用相应的回调方法
 */
@Documented
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscription {
    /**
     * Subscription名字，推荐保持唯一（默认可用类名）
     */
    String value() default "";
}