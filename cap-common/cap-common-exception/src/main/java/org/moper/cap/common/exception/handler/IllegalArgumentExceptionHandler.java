package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * IllegalArgumentException 处理器。
 */
@Priority(100)
@Slf4j
public class IllegalArgumentExceptionHandler extends AbstractExceptionHandler<IllegalArgumentException> {

    public IllegalArgumentExceptionHandler() {
        super(IllegalArgumentException.class);
    }

    @Override
    public void handle(IllegalArgumentException exception) {
        log.error("非法参数异常: {}", exception.getMessage(), exception);
    }
}
