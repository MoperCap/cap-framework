package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * RuntimeException 处理器。
 */
@Priority(100)
@Slf4j
public class RuntimeExceptionHandler extends AbstractExceptionHandler<RuntimeException> {

    public RuntimeExceptionHandler() {
        super(RuntimeException.class);
    }

    @Override
    public void handle(RuntimeException exception) {
        log.error("运行时异常: {}", exception.getMessage(), exception);
    }
}
