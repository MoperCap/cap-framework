package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

import java.util.concurrent.TimeoutException;

/**
 * TimeoutException 处理器。
 */
@Priority(100)
@Slf4j
public class TimeoutExceptionHandler extends AbstractExceptionHandler<TimeoutException> {

    public TimeoutExceptionHandler() {
        super(TimeoutException.class);
    }

    @Override
    public void handle(TimeoutException exception) {
        log.error("超时异常: {}", exception.getMessage(), exception);
    }
}
