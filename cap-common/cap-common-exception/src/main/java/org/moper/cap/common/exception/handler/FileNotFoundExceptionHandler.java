package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.ExceptionHandler;
import org.moper.cap.common.priority.Priority;

import java.io.FileNotFoundException;

/**
 * FileNotFoundException 处理器。
 */
@Priority(70)
@Slf4j
public class FileNotFoundExceptionHandler implements ExceptionHandler<FileNotFoundException> {

    @Override
    public Class<FileNotFoundException> getExceptionType() {
        return FileNotFoundException.class;
    }

    @Override
    public void handle(FileNotFoundException exception) {
        log.error("文件未找到异常: {}", exception.getMessage(), exception);
    }
}
