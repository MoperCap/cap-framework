package org.moper.cap.bean.annotation;

import java.lang.annotation.*;


/**
 * 依赖注入注解。
 *
 * @deprecated 请迁移至 {@link Autowired}（字段/参数注入）和 {@link Qualifier}（指定名称注入）。
 *             该注解保留以兼容旧代码，将在未来版本中移除。
 */
@Deprecated
@Documented
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {

    /**
     * 指定注入的 Bean 名称，默认为空字符串，表示根据类型进行注入
     */
    String value() default "";
}
