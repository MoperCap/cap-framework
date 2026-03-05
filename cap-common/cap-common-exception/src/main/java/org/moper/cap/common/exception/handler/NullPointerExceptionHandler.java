package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * NullPointerException 处理器。
 */
@Priority(60)
@Slf4j
public class NullPointerExceptionHandler implements ExceptionHandler<NullPointerException> {

    @Override
    public Class<NullPointerException> getExceptionType() {
        return NullPointerException.class;
    }

    @Override
    public void handle(NullPointerException exception) {
        log.error("空指针异常: {}", exception.getMessage(), exception);
    }
}
