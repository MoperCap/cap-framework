package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * ArrayIndexOutOfBoundsException 处理器。
 */
@Priority(60)
@Slf4j
public class ArrayIndexOutOfBoundsExceptionHandler implements ExceptionHandler<ArrayIndexOutOfBoundsException> {

    @Override
    public Class<ArrayIndexOutOfBoundsException> getExceptionType() {
        return ArrayIndexOutOfBoundsException.class;
    }

    @Override
    public void handle(ArrayIndexOutOfBoundsException exception) {
        log.error("数组越界异常: {}", exception.getMessage(), exception);
    }
}
