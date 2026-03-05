package org.moper.cap.web.exception;

/**
 * HTTP 400 Bad Request 异常。
 */
public class BadRequestException extends RuntimeException {

    private final int statusCode;

    public BadRequestException(String message) {
        super(message);
        this.statusCode = 400;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
