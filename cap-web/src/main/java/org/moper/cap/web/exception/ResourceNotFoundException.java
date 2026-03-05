package org.moper.cap.web.exception;

/**
 * HTTP 404 Not Found 异常。
 */
public class ResourceNotFoundException extends RuntimeException {

    private final int statusCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.statusCode = 404;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
