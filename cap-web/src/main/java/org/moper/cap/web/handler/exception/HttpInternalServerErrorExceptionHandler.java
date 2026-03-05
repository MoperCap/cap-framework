package org.moper.cap.web.handler.exception;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.web.exception.InternalServerErrorException;

/**
 * HTTP 500 Internal Server Error 异常处理器。
 */
@Priority(100)
@Slf4j
public class HttpInternalServerErrorExceptionHandler extends AbstractExceptionHandler<InternalServerErrorException> {

    public HttpInternalServerErrorExceptionHandler() {
        super(InternalServerErrorException.class);
    }

    @Override
    public void handle(InternalServerErrorException exception) {
        log.error("HTTP 500 Internal Server Error: {}", exception.getMessage(), exception);
    }
}
