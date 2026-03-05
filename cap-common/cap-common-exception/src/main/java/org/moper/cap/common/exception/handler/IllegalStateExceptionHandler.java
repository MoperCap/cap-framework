package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * IllegalStateException 处理器。
 */
@Priority(100)
@Slf4j
public class IllegalStateExceptionHandler extends AbstractExceptionHandler<IllegalStateException> {

    public IllegalStateExceptionHandler() {
        super(IllegalStateException.class);
    }

    @Override
    public void handle(IllegalStateException exception) {
        log.error("非法状态异常: {}", exception.getMessage(), exception);
    }
}
