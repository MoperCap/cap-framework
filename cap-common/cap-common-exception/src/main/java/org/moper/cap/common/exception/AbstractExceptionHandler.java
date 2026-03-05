package org.moper.cap.common.exception;

/**
 * 异常处理器抽象类，提供了异常类型的基本实现，子类只需实现处理逻辑。
 *
 * @param <E> 异常类型
 */
public abstract class AbstractExceptionHandler<E extends Throwable> implements ExceptionHandler<E> {
    private final Class<E> exceptionType;

    public AbstractExceptionHandler(Class<E> exceptionType) {
        this.exceptionType = exceptionType;
    }

    /**
     * 处理的异常类型
     */
    @Override
    public Class<E> getExceptionType() {
        return exceptionType;
    }
}
