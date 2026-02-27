package org.moper.cap.context.annotation;

import java.lang.annotation.*;

/**
 * 标注一个类为框架管理的组件，使其在组件扫描时被自动发现并注册为 Bean
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    /**
     * 属性订阅者注解 </br>
     * 标注在类的字段上，表示该字段会监听某个属性的变更事件，并在属性变更时调用指定的回调方法
     */
    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Subscriber {
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

    /**
     * 属性订阅者客户端注解 </br>
     * 标注在类上，框架会为该类生成一个 PropertySubscription 实例，负责监听属性变更事件并调用相应的回调方法
     */
    @Documented
    @Component
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Subscription {
        /**
         * Subscription名字，推荐保持唯一（默认可用类名）
         */
        String value() default "";
    }
}
