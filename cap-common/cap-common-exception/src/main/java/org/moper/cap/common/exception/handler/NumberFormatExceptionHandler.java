package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * NumberFormatException 处理器。
 */
@Priority(100)
@Slf4j
public class NumberFormatExceptionHandler extends AbstractExceptionHandler<NumberFormatException> {

    public NumberFormatExceptionHandler() {
        super(NumberFormatException.class);
    }

    @Override
    public void handle(NumberFormatException exception) {
        log.error("数字格式异常: {}", exception.getMessage(), exception);
    }
}
