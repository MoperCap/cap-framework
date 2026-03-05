package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * ClassCastException 处理器。
 */
@Priority(50)
@Slf4j
public class ClassCastExceptionHandler implements ExceptionHandler<ClassCastException> {

    @Override
    public Class<ClassCastException> getExceptionType() {
        return ClassCastException.class;
    }

    @Override
    public void handle(ClassCastException exception) {
        log.error("类型转换异常: {}", exception.getMessage(), exception);
    }
}
