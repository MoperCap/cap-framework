package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * RuntimeException 处理器。
 */
@Priority(10)
@Slf4j
public class RuntimeExceptionHandler implements ExceptionHandler<RuntimeException> {

    @Override
    public Class<RuntimeException> getExceptionType() {
        return RuntimeException.class;
    }

    @Override
    public void handle(RuntimeException exception) {
        log.error("运行时异常: {}", exception.getMessage(), exception);
    }
}
