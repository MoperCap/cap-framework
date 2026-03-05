package org.moper.cap.web.exception;

/**
 * HTTP 405 Method Not Allowed 异常。
 */
public class MethodNotAllowedException extends RuntimeException {

    private final int statusCode;

    public MethodNotAllowedException(String message) {
        super(message);
        this.statusCode = 405;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
