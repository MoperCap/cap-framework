package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * 通用 Exception 处理器。
 */
@Priority(5)
@Slf4j
public class ExceptionHandler_Impl implements ExceptionHandler<Exception> {

    @Override
    public Class<Exception> getExceptionType() {
        return Exception.class;
    }

    @Override
    public void handle(Exception exception) {
        log.error("异常: {}", exception.getMessage(), exception);
    }
}
