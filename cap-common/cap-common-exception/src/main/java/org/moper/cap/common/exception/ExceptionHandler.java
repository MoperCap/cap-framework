package org.moper.cap.common.exception;

/**
 * 异常处理器 </br>
 * 负责处理特定类型的异常，支持SPI方式自动发现。
 *
 * @param <E> 异常类型
 */
public interface ExceptionHandler<E extends Throwable> {

    /**
     * 处理的异常类型
     */
    Class<E> getExceptionType();

    /**
     * 处理异常逻辑。
     */
    void handle(E exception);
}
