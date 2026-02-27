package org.moper.cap.data.annotation;

import org.moper.cap.context.annotation.Component;

import java.lang.annotation.*;

/**
 * {@code @Component}  的语义化别名，标注数据访问层组件
 */
@Documented
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repository {
}
