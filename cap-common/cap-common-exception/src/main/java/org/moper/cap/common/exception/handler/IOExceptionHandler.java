package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

import java.io.IOException;

/**
 * IOException 处理器。
 */
@Priority(40)
@Slf4j
public class IOExceptionHandler implements ExceptionHandler<IOException> {

    @Override
    public Class<IOException> getExceptionType() {
        return IOException.class;
    }

    @Override
    public void handle(IOException exception) {
        log.error("IO异常: {}", exception.getMessage(), exception);
    }
}
