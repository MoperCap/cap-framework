package org.moper.cap.web.annotation;

import org.moper.cap.bean.annotation.Capper;

import java.lang.annotation.*;

/**
 * @Component 的语义化别名，标注服务层组件
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Capper
public @interface Service {
}
