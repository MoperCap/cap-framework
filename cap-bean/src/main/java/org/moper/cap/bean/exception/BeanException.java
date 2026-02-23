package org.moper.cap.bean.exception;

/**
 * cap-bean 模块所有异常的根类（非受检）。
 *
 * <p>直接继承 {@link RuntimeException} 而非 {@code CapFrameworkRuntimeException}，
 * 原因是当前 {@code CapFrameworkRuntimeException} 继承自受检的 {@code CapFrameworkException}，
 * 其非受检语义存在误导。此处独立继承以保证异常体系语义的正确性。
 */
public class BeanException extends RuntimeException {

    public BeanException(String message) {
        super(message);
    }

    public BeanException(String message, Throwable cause) {
        super(message, cause);
    }
}