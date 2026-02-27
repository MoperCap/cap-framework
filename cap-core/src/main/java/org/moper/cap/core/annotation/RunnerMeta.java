package org.moper.cap.core.annotation;

import org.moper.cap.core.runner.RunnerType;

import java.lang.annotation.*;

/**
 * Runner元信息注解 </br>
 *
 * 用于标注Runner类的元信息，框架会根据RunnerMeta注解中的type和order属性来决定Runner的执行顺序
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunnerMeta {

    /**
     * Runner类型 </br>
     *
     * Runner类型决定了Runner的执行时机和作用范围，框架会根据RunnerMeta注解中的type属性来决定Runner的执行顺序
     * @return Runner类型，默认为RunnerType.EXTENSION
     */
    RunnerType type() default RunnerType.EXTENSION;

    /**
     * 同类型Runner的优先级，数值越小优先执行 </br>
     *
     * @return 同类型Runner的优先级，默认为Integer.MAX_VALUE
     */
    int order() default Integer.MAX_VALUE;

    /**
     * Runner名字，推荐保持唯一（但并非强制） </br>
     *
     * @return Runner名字，默认为空字符串
     */
    String name() default "";

    /**
     * Runner相关描述 </br>
     *
     * @return Runner相关描述，默认为空字符串
     */
    String description() default "";
}
