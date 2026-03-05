package org.moper.cap.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.common.exception.AbstractExceptionHandler;
import org.moper.cap.common.priority.Priority;

import java.io.FileNotFoundException;

/**
 * FileNotFoundException 处理器。
 */
@Priority(100)
@Slf4j
public class FileNotFoundExceptionHandler extends AbstractExceptionHandler<FileNotFoundException> {

    public FileNotFoundExceptionHandler() {
        super(FileNotFoundException.class);
    }

    @Override
    public void handle(FileNotFoundException exception) {
        log.error("文件未找到异常: {}", exception.getMessage(), exception);
    }
}
