package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * NullPointerException 处理器。
 */
@Priority(100)
@Slf4j
public class NullPointerExceptionHandler extends AbstractExceptionHandler<NullPointerException> {

    public NullPointerExceptionHandler() {
        super(NullPointerException.class);
    }

    @Override
    public void handle(NullPointerException exception) {
        log.error("空指针异常: {}", exception.getMessage(), exception);
    }
}
