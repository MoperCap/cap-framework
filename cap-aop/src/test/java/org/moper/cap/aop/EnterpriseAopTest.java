package org.moper.cap.aop;

import org.junit.jupiter.api.Test;
import org.moper.cap.aop.annotation.After;
import org.moper.cap.aop.annotation.AfterThrowing;
import org.moper.cap.aop.annotation.Around;
import org.moper.cap.aop.annotation.Aspect;
import org.moper.cap.aop.annotation.Before;
import org.moper.cap.aop.interceptor.AopBeanInterceptor;
import org.moper.cap.aop.model.JoinPoint;
import org.moper.cap.aop.model.MethodSignature;
import org.moper.cap.aop.model.ProceedingJoinPoint;
import org.moper.cap.aop.pointcut.AnnotationPointcut;
import org.moper.cap.aop.pointcut.CompositePointcut;
import org.moper.cap.aop.pointcut.MethodSignaturePointcut;
import org.moper.cap.aop.pointcut.Pointcut;
import org.moper.cap.aop.pointcut.PointcutParser;
import org.moper.cap.aop.proxy.Advisor;
import org.moper.cap.bean.container.BeanContainer;
import org.moper.cap.bean.container.impl.DefaultBeanContainer;
import org.moper.cap.bean.definition.BeanDefinition;
import org.moper.cap.bean.interceptor.BeanInterceptor;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the enterprise AOP features: JoinPoint injection, ProceedingJoinPoint,
 * AfterThrowing, Pointcut hierarchy, PointcutParser, and CompositePointcut.
 */
public class EnterpriseAopTest {

    // =========================================================================
    // Test fixtures
    // =========================================================================

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface Logged {}

    interface CalculatorService {
        int add(int a, int b);
        int divide(int a, int b);
    }

    @Logged
    public static class CalculatorServiceImpl implements CalculatorService {
        @Override
        public int add(int a, int b) { return a + b; }

        @Override
        public int divide(int a, int b) {
            if (b == 0) throw new ArithmeticException("/ by zero");
            return a / b;
        }
    }

    // =========================================================================
    // JoinPoint / ProceedingJoinPoint injection tests
    // =========================================================================

    @Aspect
    public static class JoinPointCapturingAspect {
        JoinPoint capturedJoinPoint;
        boolean beforeFired;
        boolean afterFired;

        @Before("org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add")
        public void beforeAdd(JoinPoint jp) {
            capturedJoinPoint = jp;
            beforeFired = true;
        }

        @After("org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add")
        public void afterAdd(JoinPoint jp) {
            afterFired = true;
        }
    }

    @Test
    void testJoinPointInjection() throws Exception {
        JoinPointCapturingAspect aspect = new JoinPointCapturingAspect();
        List<Advisor> advisors = buildAdvisors(aspect, JoinPointCapturingAspect.class);

        CalculatorServiceImpl target = new CalculatorServiceImpl();
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        CalculatorService proxy = (CalculatorService) interceptor.afterPropertyInjection(target, null);

        int result = proxy.add(3, 4);

        assertEquals(7, result);
        assertTrue(aspect.beforeFired);
        assertTrue(aspect.afterFired);
        assertNotNull(aspect.capturedJoinPoint);
        assertEquals(target, aspect.capturedJoinPoint.getTarget());
        assertArrayEquals(new Object[]{3, 4}, aspect.capturedJoinPoint.getArgs());

        MethodSignature sig = aspect.capturedJoinPoint.getSignature();
        assertNotNull(sig);
        assertEquals("add", sig.getName());
        assertEquals(int.class, sig.getReturnType());
    }

    // =========================================================================
    // @Around with ProceedingJoinPoint and return value modification
    // =========================================================================

    @Aspect
    public static class AroundModifyingAspect {
        @Around("org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add")
        public Object doubleResult(ProceedingJoinPoint pjp) throws Throwable {
            Object original = pjp.proceed();
            return ((Integer) original) * 2;
        }
    }

    @Test
    void testAroundReturnValueModification() throws Exception {
        AroundModifyingAspect aspect = new AroundModifyingAspect();
        List<Advisor> advisors = buildAdvisors(aspect, AroundModifyingAspect.class);

        CalculatorServiceImpl target = new CalculatorServiceImpl();
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        CalculatorService proxy = (CalculatorService) interceptor.afterPropertyInjection(target, null);

        // 3 + 4 = 7, doubled = 14
        assertEquals(14, proxy.add(3, 4));
    }

    @Test
    void testAroundProceedWithModifiedArgs() throws Exception {
        @Aspect
        class AroundArgModifyingAspect {
            @Around("org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add")
            public Object modifyArgs(ProceedingJoinPoint pjp) throws Throwable {
                Object[] args = pjp.getArgs();
                return pjp.proceed(new Object[]{((Integer) args[0]) + 10, args[1]});
            }
        }

        AroundArgModifyingAspect aspect = new AroundArgModifyingAspect();
        List<Advisor> advisors = buildAdvisors(aspect, AroundArgModifyingAspect.class);

        CalculatorServiceImpl target = new CalculatorServiceImpl();
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        CalculatorService proxy = (CalculatorService) interceptor.afterPropertyInjection(target, null);

        // (3+10) + 4 = 17
        assertEquals(17, proxy.add(3, 4));
    }

    // =========================================================================
    // @AfterThrowing tests
    // =========================================================================

    @Aspect
    public static class ExceptionCatchingAspect {
        Throwable caughtException;
        JoinPoint capturedJoinPoint;
        boolean afterThrowingFired;

        @AfterThrowing(
            pointcut = "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.divide",
            throwing = "ex"
        )
        public void onDivideError(JoinPoint jp, ArithmeticException ex) {
            afterThrowingFired = true;
            caughtException = ex;
            capturedJoinPoint = jp;
        }
    }

    @Test
    void testAfterThrowingOnMatchingException() throws Exception {
        ExceptionCatchingAspect aspect = new ExceptionCatchingAspect();
        List<Advisor> advisors = buildAdvisors(aspect, ExceptionCatchingAspect.class);

        CalculatorServiceImpl target = new CalculatorServiceImpl();
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        CalculatorService proxy = (CalculatorService) interceptor.afterPropertyInjection(target, null);

        assertThrows(ArithmeticException.class, () -> proxy.divide(10, 0));

        assertTrue(aspect.afterThrowingFired);
        assertNotNull(aspect.caughtException);
        assertNotNull(aspect.capturedJoinPoint);
    }

    @Test
    void testAfterThrowingNotFiredOnSuccess() throws Exception {
        ExceptionCatchingAspect aspect = new ExceptionCatchingAspect();
        List<Advisor> advisors = buildAdvisors(aspect, ExceptionCatchingAspect.class);

        CalculatorServiceImpl target = new CalculatorServiceImpl();
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        CalculatorService proxy = (CalculatorService) interceptor.afterPropertyInjection(target, null);

        assertEquals(5, proxy.divide(10, 2));
        assertFalse(aspect.afterThrowingFired);
    }

    @Test
    void testAfterThrowingExceptionIsRethrown() throws Exception {
        @Aspect
        class AnyExceptionAspect {
            @AfterThrowing("org.moper.cap.aop.EnterpriseAopTest$CalculatorService.divide")
            public void onError() {}
        }

        AnyExceptionAspect aspect = new AnyExceptionAspect();
        List<Advisor> advisors = buildAdvisors(aspect, AnyExceptionAspect.class);

        CalculatorServiceImpl target = new CalculatorServiceImpl();
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        CalculatorService proxy = (CalculatorService) interceptor.afterPropertyInjection(target, null);

        // Exception must still propagate to caller
        assertThrows(ArithmeticException.class, () -> proxy.divide(5, 0));
    }

    @Test
    void testAfterThrowingNotFiredForNonMatchingType() throws Exception {
        @Aspect
        class NullPointerAspect {
            boolean fired = false;

            @AfterThrowing("org.moper.cap.aop.EnterpriseAopTest$CalculatorService.divide")
            public void onNpe(NullPointerException ex) { fired = true; }
        }

        NullPointerAspect aspect = new NullPointerAspect();
        List<Advisor> advisors = buildAdvisors(aspect, NullPointerAspect.class);

        CalculatorServiceImpl target = new CalculatorServiceImpl();
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        CalculatorService proxy = (CalculatorService) interceptor.afterPropertyInjection(target, null);

        // ArithmeticException does not match NullPointerException – advisor should be skipped
        assertThrows(ArithmeticException.class, () -> proxy.divide(5, 0));
        assertFalse(aspect.fired);
    }

    // =========================================================================
    // Pointcut tests
    // =========================================================================

    @Test
    void testMethodSignaturePointcut() throws Exception {
        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);
        Method divideMethod = CalculatorService.class.getMethod("divide", int.class, int.class);

        Pointcut pc = new MethodSignaturePointcut(
                "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add");

        assertTrue(pc.matches(addMethod, CalculatorServiceImpl.class));
        assertFalse(pc.matches(divideMethod, CalculatorServiceImpl.class));
    }

    @Test
    void testAnnotationPointcutTarget() throws Exception {
        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);

        Pointcut pc = new AnnotationPointcut(AnnotationPointcut.Mode.TARGET,
                "org.moper.cap.aop.EnterpriseAopTest$Logged");

        assertTrue(pc.matches(addMethod, CalculatorServiceImpl.class));
        assertFalse(pc.matches(addMethod, Object.class));
    }

    @Test
    void testCompositePointcutOr() throws Exception {
        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);
        Method divideMethod = CalculatorService.class.getMethod("divide", int.class, int.class);

        Pointcut matchAdd = new MethodSignaturePointcut(
                "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add");
        Pointcut matchDivide = new MethodSignaturePointcut(
                "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.divide");

        Pointcut combined = new CompositePointcut(CompositePointcut.Operator.OR, matchAdd, matchDivide);

        assertTrue(combined.matches(addMethod, CalculatorServiceImpl.class));
        assertTrue(combined.matches(divideMethod, CalculatorServiceImpl.class));
    }

    @Test
    void testCompositePointcutAnd() throws Exception {
        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);

        Pointcut matchTarget = new AnnotationPointcut(AnnotationPointcut.Mode.TARGET,
                "org.moper.cap.aop.EnterpriseAopTest$Logged");
        Pointcut matchAdd = new MethodSignaturePointcut(
                "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add");

        Pointcut combined = new CompositePointcut(CompositePointcut.Operator.AND, matchTarget, matchAdd);

        assertTrue(combined.matches(addMethod, CalculatorServiceImpl.class));
        // With Object (not @Logged), AND should fail
        assertFalse(combined.matches(addMethod, Object.class));
    }

    // =========================================================================
    // PointcutParser tests
    // =========================================================================

    @Test
    void testPointcutParserMethodSignature() throws Exception {
        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);
        Pointcut pc = PointcutParser.parse(
                "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add");

        assertTrue(pc.matches(addMethod, CalculatorServiceImpl.class));
        assertInstanceOf(MethodSignaturePointcut.class, pc);
    }

    @Test
    void testPointcutParserAnnotationMethod() throws Exception {
        Pointcut pc = PointcutParser.parse(
                "@target(org.moper.cap.aop.EnterpriseAopTest$Logged)");
        assertInstanceOf(AnnotationPointcut.class, pc);

        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);
        assertTrue(pc.matches(addMethod, CalculatorServiceImpl.class));
    }

    @Test
    void testPointcutParserOrExpression() throws Exception {
        Pointcut pc = PointcutParser.parse(
                "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add"
                        + " || "
                        + "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.divide");

        assertInstanceOf(CompositePointcut.class, pc);

        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);
        Method divideMethod = CalculatorService.class.getMethod("divide", int.class, int.class);

        assertTrue(pc.matches(addMethod, CalculatorServiceImpl.class));
        assertTrue(pc.matches(divideMethod, CalculatorServiceImpl.class));
    }

    @Test
    void testPointcutParserAndExpression() throws Exception {
        Pointcut pc = PointcutParser.parse(
                "@target(org.moper.cap.aop.EnterpriseAopTest$Logged)"
                        + " && "
                        + "org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add");

        assertInstanceOf(CompositePointcut.class, pc);

        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);
        assertTrue(pc.matches(addMethod, CalculatorServiceImpl.class));
        // Without @Logged target, AND should fail
        assertFalse(pc.matches(addMethod, Object.class));
    }

    @Test
    void testPointcutParserNegation() throws Exception {
        Pointcut pc = PointcutParser.parse(
                "!org.moper.cap.aop.EnterpriseAopTest$CalculatorService.add");

        Method addMethod = CalculatorService.class.getMethod("add", int.class, int.class);
        Method divideMethod = CalculatorService.class.getMethod("divide", int.class, int.class);

        assertFalse(pc.matches(addMethod, CalculatorServiceImpl.class));
        assertTrue(pc.matches(divideMethod, CalculatorServiceImpl.class));
    }

    // =========================================================================
    // MethodSignature tests
    // =========================================================================

    @Test
    void testMethodSignatureInfo() throws Exception {
        JoinPointCapturingAspect aspect = new JoinPointCapturingAspect();
        List<Advisor> advisors = buildAdvisors(aspect, JoinPointCapturingAspect.class);

        CalculatorServiceImpl target = new CalculatorServiceImpl();
        BeanInterceptor interceptor = new AopBeanInterceptor(advisors);
        CalculatorService proxy = (CalculatorService) interceptor.afterPropertyInjection(target, null);

        proxy.add(1, 2);

        MethodSignature sig = aspect.capturedJoinPoint.getSignature();
        assertNotNull(sig);
        assertEquals("add", sig.getName());
        assertEquals(int.class, sig.getReturnType());
        assertArrayEquals(new Class[]{int.class, int.class}, sig.getParameterTypes());
        assertNotNull(sig.getMethod());
        assertNotNull(sig.getDeclaringType());
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private static List<Advisor> buildAdvisors(Object aspect, Class<?> aspectClass) {
        List<Advisor> advisors = new ArrayList<>();
        for (Method m : aspectClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Before.class)) {
                advisors.add(new Advisor(Advisor.Type.BEFORE,
                        m.getAnnotation(Before.class).value(), aspect, m));
            }
            if (m.isAnnotationPresent(Around.class)) {
                advisors.add(new Advisor(Advisor.Type.AROUND,
                        m.getAnnotation(Around.class).value(), aspect, m));
            }
            if (m.isAnnotationPresent(After.class)) {
                advisors.add(new Advisor(Advisor.Type.AFTER,
                        m.getAnnotation(After.class).value(), aspect, m));
            }
            if (m.isAnnotationPresent(AfterThrowing.class)) {
                AfterThrowing at = m.getAnnotation(AfterThrowing.class);
                String expr = at.pointcut().isEmpty() ? at.value() : at.pointcut();
                advisors.add(new Advisor(Advisor.Type.AFTER_THROWING, expr, aspect, m));
            }
        }
        return advisors;
    }
}
