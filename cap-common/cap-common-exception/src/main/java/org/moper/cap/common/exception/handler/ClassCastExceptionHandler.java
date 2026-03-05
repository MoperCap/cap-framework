package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * ClassCastException 处理器。
 */
@Priority(100)
@Slf4j
public class ClassCastExceptionHandler extends AbstractExceptionHandler<ClassCastException> {

    public ClassCastExceptionHandler() {
        super(ClassCastException.class);
    }

    @Override
    public void handle(ClassCastException exception) {
        log.error("类型转换异常: {}", exception.getMessage(), exception);
    }
}
