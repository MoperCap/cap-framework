package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

/**
 * 兜底异常处理器，处理所有未被精确匹配的异常。
 */
@Priority(100)
@Slf4j
public class ThrowableHandler extends AbstractExceptionHandler<Throwable> {

    public ThrowableHandler() {
        super(Throwable.class);
    }

    @Override
    public void handle(Throwable exception) {
        log.error("未分类的异常: {}", exception.getMessage(), exception);
    }
}
