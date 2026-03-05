package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * NumberFormatException 处理器。
 */
@Priority(50)
@Slf4j
public class NumberFormatExceptionHandler implements ExceptionHandler<NumberFormatException> {

    @Override
    public Class<NumberFormatException> getExceptionType() {
        return NumberFormatException.class;
    }

    @Override
    public void handle(NumberFormatException exception) {
        log.error("数字格式异常: {}", exception.getMessage(), exception);
    }
}
