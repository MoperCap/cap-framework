package org.moper.cap.aop.annotation;
import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {
    /**
     * 切点表达式
     */
    String value();
}