package org.moper.cap.web.handler.exception;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.web.exception.ResourceNotFoundException;

/**
 * HTTP 404 Not Found 异常处理器。
 */
@Priority(100)
@Slf4j
public class HttpNotFoundExceptionHandler extends AbstractExceptionHandler<ResourceNotFoundException> {

    public HttpNotFoundExceptionHandler() {
        super(ResourceNotFoundException.class);
    }

    @Override
    public void handle(ResourceNotFoundException exception) {
        log.warn("HTTP 404 Not Found: {}", exception.getMessage(), exception);
    }
}
