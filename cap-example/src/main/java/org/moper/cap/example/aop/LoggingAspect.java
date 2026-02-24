package org.moper.cap.example.aop;

import org.moper.cap.aop.annotation.Aspect;
import org.moper.cap.aop.annotation.Before;

@Aspect
public class LoggingAspect {
    @Before("org.moper.cap.example.bean.ServiceDemo.greet")
    public void beforeGreet(String name) {
        System.out.println("AOP: before greet, name = " + name);
    }
}