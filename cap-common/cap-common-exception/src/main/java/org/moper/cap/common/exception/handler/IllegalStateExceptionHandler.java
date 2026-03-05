package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * IllegalStateException 处理器。
 */
@Priority(50)
@Slf4j
public class IllegalStateExceptionHandler implements ExceptionHandler<IllegalStateException> {

    @Override
    public Class<IllegalStateException> getExceptionType() {
        return IllegalStateException.class;
    }

    @Override
    public void handle(IllegalStateException exception) {
        log.error("非法状态异常: {}", exception.getMessage(), exception);
    }
}
