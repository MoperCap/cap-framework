package org.moper.cap.web.annotation;

import org.moper.cap.context.annotation.Component;

import java.lang.annotation.*;

/**
 * @Component 的语义化别名，标注服务层组件
 */
@Documented
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
}
