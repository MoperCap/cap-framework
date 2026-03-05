package org.moper.cap.web.annotation.router;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConnectRouter {

    String value() default "";
}
