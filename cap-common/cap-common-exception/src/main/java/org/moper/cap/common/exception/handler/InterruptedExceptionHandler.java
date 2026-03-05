package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * InterruptedException 处理器。
 */
@Priority(100)
@Slf4j
public class InterruptedExceptionHandler extends AbstractExceptionHandler<InterruptedException> {

    public InterruptedExceptionHandler() {
        super(InterruptedException.class);
    }

    @Override
    public void handle(InterruptedException exception) {
        log.error("线程中断异常: {}", exception.getMessage(), exception);
        Thread.currentThread().interrupt();
    }
}
