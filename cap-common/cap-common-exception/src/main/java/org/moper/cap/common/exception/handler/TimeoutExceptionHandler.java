package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

import java.util.concurrent.TimeoutException;

/**
 * TimeoutException 处理器。
 */
@Priority(40)
@Slf4j
public class TimeoutExceptionHandler implements ExceptionHandler<TimeoutException> {

    @Override
    public Class<TimeoutException> getExceptionType() {
        return TimeoutException.class;
    }

    @Override
    public void handle(TimeoutException exception) {
        log.error("超时异常: {}", exception.getMessage(), exception);
    }
}
