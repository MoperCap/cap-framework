package org.moper.cap.web.annotation.route;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OptionsRouter {

    String value() default "";
}
