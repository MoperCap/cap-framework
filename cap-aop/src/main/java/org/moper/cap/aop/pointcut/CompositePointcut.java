package org.moper.cap.aop.pointcut;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Pointcut} that combines multiple pointcuts with a logical operator.
 *
 * <p>Supports {@link Operator#AND} (all must match) and {@link Operator#OR} (any must match).
 */
public class CompositePointcut implements Pointcut {

    public enum Operator { AND, OR }

    private final List<Pointcut> pointcuts;
    private final Operator operator;

    public CompositePointcut(List<Pointcut> pointcuts, Operator operator) {
        this.pointcuts = pointcuts;
        this.operator = operator;
    }

    public CompositePointcut(Operator operator, Pointcut... pointcuts) {
        this.pointcuts = Arrays.asList(pointcuts);
        this.operator = operator;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (operator == Operator.OR) {
            for (Pointcut p : pointcuts) {
                if (p.matches(method, targetClass)) return true;
            }
            return false;
        } else {
            for (Pointcut p : pointcuts) {
                if (!p.matches(method, targetClass)) return false;
            }
            return true;
        }
    }

    @Override
    public String toString() {
        String sep = operator == Operator.OR ? " || " : " && ";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < pointcuts.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(pointcuts.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}
