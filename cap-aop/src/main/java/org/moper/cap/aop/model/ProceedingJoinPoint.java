package org.moper.cap.aop.model;

/**
 * Extension of {@link JoinPoint} for {@code @Around} advice.
 *
 * <p>Allows the advice to proceed with the original method invocation via {@link #proceed()},
 * optionally replacing the method arguments.
 */
public interface ProceedingJoinPoint extends JoinPoint {

    /**
     * Proceeds with the original method invocation using the current arguments.
     *
     * @return the return value of the original method
     * @throws Throwable if the original method throws
     */
    Object proceed() throws Throwable;

    /**
     * Proceeds with the original method invocation using the supplied arguments.
     *
     * @param args the arguments to use for the invocation
     * @return the return value of the original method
     * @throws Throwable if the original method throws
     */
    Object proceed(Object[] args) throws Throwable;
}
