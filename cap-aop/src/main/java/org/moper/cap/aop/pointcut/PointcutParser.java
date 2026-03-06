package org.moper.cap.aop.pointcut;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a pointcut expression string into a {@link Pointcut} instance.
 *
 * <p>Supported formats:
 * <ul>
 *   <li>{@code @method(fully.qualified.Annotation)} – {@link AnnotationPointcut} (method mode)</li>
 *   <li>{@code @target(fully.qualified.Annotation)} – {@link AnnotationPointcut} (target mode)</li>
 *   <li>{@code fully.qualified.ClassName.methodName} – {@link MethodSignaturePointcut}</li>
 *   <li>{@code A || B} – {@link CompositePointcut} with OR operator</li>
 *   <li>{@code A && B} – {@link CompositePointcut} with AND operator</li>
 *   <li>{@code !A} – negated pointcut</li>
 *   <li>{@code (A)} – parenthesised expression</li>
 * </ul>
 */
public class PointcutParser {

    private PointcutParser() {}

    /**
     * Parses the given pointcut expression and returns a corresponding {@link Pointcut}.
     *
     * @param expression the pointcut expression (never {@code null})
     * @return a {@link Pointcut} that implements the expression semantics
     */
    public static Pointcut parse(String expression) {
        expression = expression.trim();

        // OR has lowest precedence – try splitting by " || " first
        List<String> orParts = splitByOperator(expression, "||");
        if (orParts.size() > 1) {
            List<Pointcut> pointcuts = new ArrayList<>();
            for (String part : orParts) {
                pointcuts.add(parse(part.trim()));
            }
            return new CompositePointcut(pointcuts, CompositePointcut.Operator.OR);
        }

        // AND has higher precedence
        List<String> andParts = splitByOperator(expression, "&&");
        if (andParts.size() > 1) {
            List<Pointcut> pointcuts = new ArrayList<>();
            for (String part : andParts) {
                pointcuts.add(parse(part.trim()));
            }
            return new CompositePointcut(pointcuts, CompositePointcut.Operator.AND);
        }

        // Strip outer parentheses
        if (expression.startsWith("(") && expression.endsWith(")")) {
            return parse(expression.substring(1, expression.length() - 1));
        }

        // Negation
        if (expression.startsWith("!")) {
            Pointcut inner = parse(expression.substring(1));
            return (method, targetClass) -> !inner.matches(method, targetClass);
        }

        // Annotation-based: @method(fqn)
        if (expression.startsWith("@method(") && expression.endsWith(")")) {
            String annotationFqn = expression.substring(8, expression.length() - 1).trim();
            return new AnnotationPointcut(AnnotationPointcut.Mode.METHOD, annotationFqn);
        }

        // Annotation-based: @target(fqn)
        if (expression.startsWith("@target(") && expression.endsWith(")")) {
            String annotationFqn = expression.substring(8, expression.length() - 1).trim();
            return new AnnotationPointcut(AnnotationPointcut.Mode.TARGET, annotationFqn);
        }

        // Default: treat as method signature
        return new MethodSignaturePointcut(expression);
    }

    /**
     * Splits {@code expression} by the given two-character operator token ({@code "||"} or
     * {@code "&&"}), respecting parenthesis depth so that nested expressions are not split.
     */
    private static List<String> splitByOperator(String expression, String operator) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        int len = expression.length();
        int opLen = operator.length();

        for (int i = 0; i <= len - opLen; i++) {
            char c = expression.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && expression.startsWith(operator, i)) {
                parts.add(expression.substring(start, i).trim());
                start = i + opLen;
                i += opLen - 1;
            }
        }
        parts.add(expression.substring(start).trim());
        if (parts.size() == 1) {
            parts.clear(); // no split occurred
        }
        return parts;
    }
}
