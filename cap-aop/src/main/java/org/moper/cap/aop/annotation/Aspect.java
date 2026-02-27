package org.moper.cap.aop.annotation;
import org.moper.cap.bean.annotation.Component;

import java.lang.annotation.*;

@Documented
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect { }