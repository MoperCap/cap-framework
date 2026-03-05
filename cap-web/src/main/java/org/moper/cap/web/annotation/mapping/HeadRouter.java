package org.moper.cap.web.annotation.mapping;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HeadRouter {

    String value() default "";
}
