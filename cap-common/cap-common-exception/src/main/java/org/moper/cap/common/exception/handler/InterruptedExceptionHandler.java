package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * InterruptedException 处理器。
 */
@Priority(40)
@Slf4j
public class InterruptedExceptionHandler implements ExceptionHandler<InterruptedException> {

    @Override
    public Class<InterruptedException> getExceptionType() {
        return InterruptedException.class;
    }

    @Override
    public void handle(InterruptedException exception) {
        log.error("线程中断异常: {}", exception.getMessage(), exception);
        Thread.currentThread().interrupt();
    }
}
