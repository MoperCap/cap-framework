package org.moper.cap.web.exception.handler;

import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * Servlet 异常处理器，处理 {@link ServletException}，返回 HTTP 500 错误。
 */
@Priority(150)
@Slf4j
public class ServletExceptionHandler extends AbstractExceptionHandler<ServletException> {

    public ServletExceptionHandler() {
        super(ServletException.class);
    }

    @Override
    public void handle(ServletException exception) {
        log.error("Servlet 异常: {}", exception.getMessage(), exception);
    }
}
