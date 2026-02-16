package org.moper.cap.bean.exception;

import org.moper.cap.core.exception.CapFrameworkRuntimeException;

public class BeanException extends CapFrameworkRuntimeException {
    public BeanException(String message) {
        super(message);
    }

    public BeanException(String message, Throwable cause) {super(message, cause);}
}
