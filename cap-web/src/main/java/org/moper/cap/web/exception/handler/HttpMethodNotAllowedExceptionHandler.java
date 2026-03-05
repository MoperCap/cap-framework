package org.moper.cap.web.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;
import org.moper.cap.web.exception.MethodNotAllowedException;

/**
 * HTTP 405 Method Not Allowed 异常处理器。
 */
@Priority(200)
@Slf4j
public class HttpMethodNotAllowedExceptionHandler extends AbstractExceptionHandler<MethodNotAllowedException> {

    public HttpMethodNotAllowedExceptionHandler() {
        super(MethodNotAllowedException.class);
    }

    @Override
    public void handle(MethodNotAllowedException exception) {
        log.warn("HTTP 405 Method Not Allowed: {}", exception.getMessage(), exception);
    }
}
