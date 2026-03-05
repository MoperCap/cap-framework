package org.moper.cap.web.handler.exception;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.web.exception.BadRequestException;

/**
 * HTTP 400 Bad Request 异常处理器。
 */
@Priority(100)
@Slf4j
public class HttpBadRequestExceptionHandler extends AbstractExceptionHandler<BadRequestException> {

    public HttpBadRequestExceptionHandler() {
        super(BadRequestException.class);
    }

    @Override
    public void handle(BadRequestException exception) {
        log.warn("HTTP 400 Bad Request: {}", exception.getMessage(), exception);
    }
}
