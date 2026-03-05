package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * IllegalArgumentException 处理器。
 */
@Priority(50)
@Slf4j
public class IllegalArgumentExceptionHandler implements ExceptionHandler<IllegalArgumentException> {

    @Override
    public Class<IllegalArgumentException> getExceptionType() {
        return IllegalArgumentException.class;
    }

    @Override
    public void handle(IllegalArgumentException exception) {
        log.error("非法参数异常: {}", exception.getMessage(), exception);
    }
}
