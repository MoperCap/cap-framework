package org.moper.cap.context;

import org.moper.cap.exception.ContextException;

/**
 * 框架运行期阶段系统上下文
 * 提供只读的Bean访问能力
 */
public interface ApplicationContext extends AutoCloseable{

    void run() throws ContextException;
}
