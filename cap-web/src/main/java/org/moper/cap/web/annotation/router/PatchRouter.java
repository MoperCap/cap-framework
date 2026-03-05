package org.moper.cap.web.annotation.router;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PatchRouter {

    String value() default "";
}
