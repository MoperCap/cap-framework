package org.moper.cap.annotation;

import java.lang.annotation.*;

/**
 * 属性订阅者注解 </br>
 * 标注在类的字段上，表示该字段会监听某个属性的变更事件，并在属性变更时调用指定的回调方法
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscriber {
    /**
     * 属性的 key，必须指定
     */
    String propertyKey();

    /**
     * 属性变更时的回调方法名，可选：void onSet(Object newVal)
     */
    String onSet() default "";

    /**
     * 属性被移除时的回调方法名，可选：void onRemoved()
     */
    String onRemoved() default "";
}