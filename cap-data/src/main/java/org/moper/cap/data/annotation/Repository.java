package org.moper.cap.data.annotation;

import org.moper.cap.bean.annotation.Capper;

import java.lang.annotation.*;

/**
 * {@code @Component}  的语义化别名，标注数据访问层组件
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Capper
public @interface Repository {
}
