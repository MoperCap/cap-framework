package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * ArrayIndexOutOfBoundsException 处理器。
 */
@Priority(100)
@Slf4j
public class ArrayIndexOutOfBoundsExceptionHandler extends AbstractExceptionHandler<ArrayIndexOutOfBoundsException> {
    public ArrayIndexOutOfBoundsExceptionHandler() {
        super(ArrayIndexOutOfBoundsException.class);
    }


    @Override
    public void handle(ArrayIndexOutOfBoundsException exception) {
        log.error("数组越界异常: {}", exception.getMessage(), exception);
    }
}
