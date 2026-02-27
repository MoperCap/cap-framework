package org.moper.cap.bean.annotation;

import org.moper.cap.bean.definition.BeanScope;

import java.lang.annotation.*;

/**
 * 标记一个类或方法为 Cap Bean 的定义
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Capper {

    /**
     * Bean 名称/别名（可支持多个）
     */
    String[] name() default {};

    /**
     * 是否为首选 Bean（当存在多个同类型 Bean 时，优先注入该 Bean）
     */
    boolean primary() default false;

    /**
     * 是否为懒加载 Bean（仅在第一次被注入时才创建实例）
     */
    boolean lazy() default false;

    /**
     * Bean 作用域（默认为单例）
     */
    BeanScope scope() default BeanScope.SINGLETON;
}
