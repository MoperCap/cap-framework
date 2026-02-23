package org.moper.cap.aop.advisor;

import lombok.Getter;

import java.lang.reflect.Method;

public class Advisor {
    public enum Type { BEFORE, AFTER, AROUND }
    @Getter
    private final Type type;
    private final String pointcut;
    @Getter
    private final Object aspectInstance;
    @Getter
    private final Method adviceMethod;

    public Advisor(Type type, String pointcut, Object aspectInstance, Method adviceMethod) {
        this.type = type;
        this.pointcut = pointcut;
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
    }

    public boolean matches(Method m) {
        String sig = m.getDeclaringClass().getName() + "." + m.getName();
        return sig.equals(pointcut);
    }

}