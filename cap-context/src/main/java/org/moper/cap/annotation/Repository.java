package org.moper.cap.annotation;

import java.lang.annotation.*;

/**
 * @Component 的语义化别名，标注数据访问层组件
 */
@Documented
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repository {
}
