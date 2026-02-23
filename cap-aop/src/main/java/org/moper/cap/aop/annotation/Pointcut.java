package org.moper.cap.aop.annotation;
import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pointcut {
    /**
     * 切点表达式，例如 "com.example.MyService.doWork"
     */
    String value();
}