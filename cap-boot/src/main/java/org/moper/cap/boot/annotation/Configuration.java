package org.moper.cap.boot.annotation;

import java.lang.annotation.*;

@Documented
@Configuration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
}
