package org.moper.cap.web.exception;

/**
 * HTTP 500 Internal Server Error 异常。
 */
public class InternalServerErrorException extends RuntimeException {

    private final int statusCode;

    public InternalServerErrorException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
